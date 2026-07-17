package com.lms.lms.service;

import com.lms.lms.dto.response.MessageRes;
import com.lms.lms.modals.Conversation;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Message;
import com.lms.lms.modals.Notification;
import com.lms.lms.modals.User;
import com.lms.lms.repo.ConversationRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.MessageRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Human-to-human messaging. Two shapes, both anchored on {@link Conversation}:
 *
 * <ul>
 *   <li>SUPPORT — a user and customer care. No fixed counterpart: the thread sits in a
 *       shared queue and any ADMIN can answer it.</li>
 *   <li>DIRECT — a student and one instructor (scoped to a course they are enrolled in)
 *       or an admin.</li>
 * </ul>
 *
 * Unrelated to {@link ChatService}, which is the Gemini AI tutor.
 */
@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    public static final String MESSAGE_QUEUE = "/queue/messages";

    // Anti-spam cap on the student side. Staff are exempt: an instructor answering a
    // cohort or an admin working the support queue legitimately sends a lot.
    private static final int STUDENT_DAILY_MESSAGE_LIMIT = 100;

    @Autowired
    private ConversationRepo conversationRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ---------------------------------------------------------------- access

    /**
     * SUPPORT threads are readable by their initiator and by any admin (shared queue).
     * DIRECT threads are readable only by the two participants — an admin who is not a
     * participant is deliberately not granted access to other people's private threads.
     */
    public boolean canAccess(Conversation conversation, User user) {
        if (conversation.getInitiator().getId().equals(user.getId())) {
            return true;
        }
        if (conversation.getRecipient() != null && conversation.getRecipient().getId().equals(user.getId())) {
            return true;
        }
        return conversation.getType() == Conversation.Type.SUPPORT && user.getRole() == User.Role.ADMIN;
    }

    public Conversation requireAccess(String conversationId, User user) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        if (!canAccess(conversation, user)) {
            throw new SecurityException("You do not have access to this conversation");
        }
        return conversation;
    }

    /**
     * Read-only moderation lookup for admins: reaches any thread, including private
     * DIRECT ones. Intentionally separate from {@link #canAccess} so it can never widen
     * who is allowed to <em>post</em> — {@link #send} still goes through canAccess, so an
     * admin reading a DIRECT thread cannot reply into it.
     */
    public Conversation getForModeration(String conversationId) {
        return conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        Conversation conversation = getForModeration(conversationId);
        messageRepo.deleteByConversationId(conversation.getId());
        conversationRepo.delete(conversation);
    }

    // ------------------------------------------------------------- creation

    /** Reuses the caller's open support thread so the queue does not fill with duplicates. */
    @Transactional
    public Conversation startSupport(User initiator, String subject) {
        return conversationRepo
                .findFirstByInitiator_IdAndTypeAndStatus(initiator.getId(), Conversation.Type.SUPPORT, Conversation.Status.OPEN)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setType(Conversation.Type.SUPPORT);
                    conversation.setInitiator(initiator);
                    conversation.setSubject(subject == null || subject.isBlank() ? "Support request" : subject);
                    conversation.setStatus(Conversation.Status.OPEN);
                    return conversationRepo.save(conversation);
                });
    }

    /**
     * Opens (or reuses) a student -> instructor/admin thread.
     *
     * For an instructor the caller must be enrolled in a course that instructor owns:
     * this is what stops students cold-messaging arbitrary instructors.
     */
    @Transactional
    public Conversation startDirect(User initiator, String recipientId, Long courseId, String subject) {
        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("recipientId is required");
        }
        if (recipientId.equals(initiator.getId())) {
            throw new IllegalArgumentException("You cannot start a conversation with yourself");
        }

        User recipient = userRepo.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        if (Boolean.TRUE.equals(recipient.getIsDeleted()) || Boolean.FALSE.equals(recipient.getIsActive())) {
            throw new IllegalArgumentException("Recipient is not available");
        }
        if (recipient.getRole() == User.Role.STUDENT) {
            throw new IllegalArgumentException("You can only message an instructor or an admin");
        }

        Courses course = null;
        if (courseId != null) {
            course = coursesRepo.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));

            if (!course.getUser().getId().equals(recipient.getId())) {
                throw new IllegalArgumentException("That instructor does not teach this course");
            }
            // the initiator is allowed through if they are enrolled, or if they own the course themselves
            boolean enrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(initiator.getId(), courseId);
            if (!enrolled && !course.getUser().getId().equals(initiator.getId())) {
                throw new IllegalArgumentException("You must be enrolled in this course to message its instructor");
            }
        } else if (recipient.getRole() == User.Role.INSTRUCTOR) {
            throw new IllegalArgumentException("courseId is required when messaging an instructor");
        }

        Courses resolvedCourse = course;
        return (resolvedCourse == null
                ? conversationRepo.findFirstByInitiator_IdAndRecipient_IdAndCourseIsNullAndType(
                initiator.getId(), recipient.getId(), Conversation.Type.DIRECT)
                : conversationRepo.findFirstByInitiator_IdAndRecipient_IdAndCourse_IdAndType(
                initiator.getId(), recipient.getId(), resolvedCourse.getId(), Conversation.Type.DIRECT))
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setType(Conversation.Type.DIRECT);
                    conversation.setInitiator(initiator);
                    conversation.setRecipient(recipient);
                    conversation.setCourse(resolvedCourse);
                    conversation.setSubject(subject == null || subject.isBlank()
                            ? (resolvedCourse == null ? "Direct message" : resolvedCourse.getTitle())
                            : subject);
                    conversation.setStatus(Conversation.Status.OPEN);
                    return conversationRepo.save(conversation);
                });
    }

    // -------------------------------------------------------------- sending

    @Transactional
    public Message send(Conversation conversation, User sender, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }
        if (!canAccess(conversation, sender)) {
            throw new SecurityException("You do not have access to this conversation");
        }
        enforceDailyLimit(sender);

        // a reply to a resolved thread reopens it rather than being rejected
        if (conversation.getStatus() == Conversation.Status.CLOSED) {
            conversation.setStatus(Conversation.Status.OPEN);
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content.trim());
        Message saved = messageRepo.save(message);

        conversation.setLastMessageAt(saved.getCreatedAt());
        conversationRepo.save(conversation);

        deliver(conversation, saved, sender);
        return saved;
    }

    /**
     * Caps how many messages a student can send per rolling 24 hours, so one account
     * cannot flood an instructor or bury the support queue.
     *
     * @throws IllegalStateException when the cap is hit; the controller maps this to 429
     */
    private void enforceDailyLimit(User sender) {
        if (sender.getRole() != User.Role.STUDENT) {
            return;
        }
        Date since = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        long sent = messageRepo.countBySender_IdAndCreatedAtAfter(sender.getId(), since);
        if (sent >= STUDENT_DAILY_MESSAGE_LIMIT) {
            throw new IllegalStateException(
                    "Daily message limit reached (" + STUDENT_DAILY_MESSAGE_LIMIT + "). Please try again later.");
        }
    }

    /** Pushes the message to the other side's socket and raises their in-app notification. */
    private void deliver(Conversation conversation, Message message, User sender) {
        MessageRes payload = MessageRes.from(message);
        String link = "/messages/" + conversation.getId();
        String preview = message.getContent().length() > 120
                ? message.getContent().substring(0, 120) + "..."
                : message.getContent();

        for (User recipient : recipientsOf(conversation, sender)) {
            try {
                messagingTemplate.convertAndSendToUser(recipient.getId(), MESSAGE_QUEUE, payload);
            } catch (Exception e) {
                log.warn("Failed to push message {} to user {}: {}", message.getId(), recipient.getId(), e.getMessage());
            }
            notificationService.notify(
                    recipient,
                    Notification.Type.NEW_MESSAGE,
                    "New message from " + sender.getName(),
                    preview,
                    link,
                    conversation.getId()
            );
        }
    }

    /** Everyone who should hear about a new message, excluding its sender. */
    private List<User> recipientsOf(Conversation conversation, User sender) {
        if (conversation.getType() == Conversation.Type.DIRECT) {
            User other = conversation.getInitiator().getId().equals(sender.getId())
                    ? conversation.getRecipient()
                    : conversation.getInitiator();
            return other == null ? List.of() : List.of(other);
        }

        // SUPPORT: a user's message goes to the whole admin queue; an admin's reply
        // goes back to the user who opened it.
        if (conversation.getInitiator().getId().equals(sender.getId())) {
            return userRepo.findByRoleAndIsActiveTrueAndIsDeletedFalseAndIsBannedFalse(User.Role.ADMIN)
                    .stream()
                    .filter(admin -> !admin.getId().equals(sender.getId()))
                    .toList();
        }
        return List.of(conversation.getInitiator());
    }

    // ------------------------------------------------------------- reading

    @Transactional
    public int markRead(Conversation conversation, User viewer) {
        return messageRepo.markReadFor(conversation.getId(), viewer.getId(), new Date());
    }

    public long unreadCount(Conversation conversation, User viewer) {
        return messageRepo.countUnreadFor(conversation.getId(), viewer.getId());
    }
}
