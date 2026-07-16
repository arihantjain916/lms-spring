package com.lms.lms.dto.response;

import com.lms.lms.modals.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class MessageRes {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Date readAt;
    private Date createdAt;

    public static MessageRes from(Message message) {
        return new MessageRes(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getSender().getAvatar(),
                message.getContent(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
