package com.lms.lms.dto.response;

import com.lms.lms.modals.Conversation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ConversationRes {
    private String id;
    private Conversation.Type type;
    private Conversation.Status status;
    private String subject;
    private String initiatorId;
    private String initiatorName;
    // null on SUPPORT threads: they are answered by whichever admin picks them up
    private String recipientId;
    private String recipientName;
    private Long courseId;
    private String courseTitle;
    private long unreadCount;
    private Date lastMessageAt;
    private Date createdAt;

    public static ConversationRes from(Conversation conversation, long unreadCount) {
        return new ConversationRes(
                conversation.getId(),
                conversation.getType(),
                conversation.getStatus(),
                conversation.getSubject(),
                conversation.getInitiator().getId(),
                conversation.getInitiator().getName(),
                conversation.getRecipient() == null ? null : conversation.getRecipient().getId(),
                conversation.getRecipient() == null ? null : conversation.getRecipient().getName(),
                conversation.getCourse() == null ? null : conversation.getCourse().getId(),
                conversation.getCourse() == null ? null : conversation.getCourse().getTitle(),
                unreadCount,
                conversation.getLastMessageAt(),
                conversation.getCreatedAt()
        );
    }
}
