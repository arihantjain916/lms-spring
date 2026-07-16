package com.lms.lms.service;

import com.lms.lms.dto.response.NotificationRes;
import com.lms.lms.modals.Notification;
import com.lms.lms.modals.User;
import com.lms.lms.repo.NotificationRepo;
import com.lms.lms.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creates in-app notifications and pushes them to the recipient over STOMP.
 *
 * Delivery is best-effort: a websocket failure never fails the caller's transaction,
 * because the row is already persisted and the client will pick it up on next fetch.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public static final String NOTIFICATION_QUEUE = "/queue/notifications";

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification notify(User user, Notification.Type type, String title, String body,
                               String link, String referenceId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setLink(link);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);

        Notification saved = notificationRepo.save(notification);
        push(user.getId(), saved);
        return saved;
    }

    /**
     * Fan-out to every user holding a role. Used to reach the shared customer-care
     * queue, where no single agent owns the conversation.
     */
    public void notifyRole(User.Role role, Notification.Type type, String title, String body,
                           String link, String referenceId, String excludeUserId) {
        List<User> recipients = userRepo.findByRoleAndIsActiveTrueAndIsDeletedFalseAndIsBannedFalse(role);
        for (User recipient : recipients) {
            if (recipient.getId().equals(excludeUserId)) {
                continue;
            }
            notify(recipient, type, title, body, link, referenceId);
        }
    }

    /**
     * Announcement fan-out. A null role targets every active user.
     *
     * Runs inline and writes one row per recipient, so on a large user base this is a slow
     * request — worth moving to @Async or a batched job if the audience grows.
     *
     * @return how many notifications were created
     */
    public int broadcast(User.Role role, Notification.Type type, String title, String body, String link) {
        List<User> recipients = role == null
                ? userRepo.findByIsActiveTrueAndIsDeletedFalseAndIsBannedFalse()
                : userRepo.findByRoleAndIsActiveTrueAndIsDeletedFalseAndIsBannedFalse(role);

        for (User recipient : recipients) {
            notify(recipient, type, title, body, link, null);
        }
        return recipients.size();
    }

    public void push(String userId, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(userId, NOTIFICATION_QUEUE, NotificationRes.from(notification));
        } catch (Exception e) {
            log.warn("Failed to push notification {} to user {}: {}", notification.getId(), userId, e.getMessage());
        }
    }
}
