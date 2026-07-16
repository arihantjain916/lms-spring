package com.lms.lms.repo;

import com.lms.lms.modals.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, String> {

    // a user keeps one open support thread at a time; reopened instead of duplicated
    Optional<Conversation> findFirstByInitiator_IdAndTypeAndStatus(String initiatorId,
                                                                   Conversation.Type type,
                                                                   Conversation.Status status);

    // an existing student<->instructor thread for a course is reused rather than duplicated
    Optional<Conversation> findFirstByInitiator_IdAndRecipient_IdAndCourse_IdAndType(String initiatorId,
                                                                                     String recipientId,
                                                                                     Long courseId,
                                                                                     Conversation.Type type);

    Optional<Conversation> findFirstByInitiator_IdAndRecipient_IdAndCourseIsNullAndType(String initiatorId,
                                                                                        String recipientId,
                                                                                        Conversation.Type type);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.initiator.id = :userId OR c.recipient.id = :userId
            """)
    Page<Conversation> findMine(@Param("userId") String userId, Pageable pageable);

    // admin moderation view, and the customer-care queue via type=SUPPORT
    @Query("""
            SELECT c FROM Conversation c
            WHERE (:type IS NULL OR c.type = :type)
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<Conversation> adminSearch(@Param("type") Conversation.Type type,
                                   @Param("status") Conversation.Status status,
                                   Pageable pageable);

    long countByTypeAndStatus(Conversation.Type type, Conversation.Status status);

    /**
     * Support threads whose newest activity is an unread message from the user:
     * i.e. tickets still waiting on customer care.
     */
    @Query("""
            SELECT count(DISTINCT c.id) FROM Conversation c
            JOIN Message m ON m.conversation = c
            WHERE c.type = :type
              AND c.status = :status
              AND m.sender.id = c.initiator.id
              AND m.readAt IS NULL
            """)
    long countAwaitingReply(@Param("type") Conversation.Type type,
                            @Param("status") Conversation.Status status);
}
