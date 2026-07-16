package com.lms.lms.repo;

import com.lms.lms.modals.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface MessageRepo extends JpaRepository<Message, String> {

    Page<Message> findByConversation_Id(String conversationId, Pageable pageable);

    // backs the anti-spam cap; counted off the messages themselves so there is no
    // separate usage counter to drift out of sync
    long countBySender_IdAndCreatedAtAfter(String senderId, Date since);

    // messages FK the conversation, so they go first when an admin deletes a thread
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Message m WHERE m.conversation.id = :conversationId")
    int deleteByConversationId(@Param("conversationId") String conversationId);

    // "unread" is always relative to the viewer: messages they did not send and nobody has read yet
    @Query("""
            SELECT count(m) FROM Message m
            WHERE m.conversation.id = :conversationId
              AND m.sender.id <> :viewerId
              AND m.readAt IS NULL
            """)
    long countUnreadFor(@Param("conversationId") String conversationId, @Param("viewerId") String viewerId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Message m SET m.readAt = :now
            WHERE m.conversation.id = :conversationId
              AND m.sender.id <> :viewerId
              AND m.readAt IS NULL
            """)
    int markReadFor(@Param("conversationId") String conversationId,
                    @Param("viewerId") String viewerId,
                    @Param("now") Date now);
}
