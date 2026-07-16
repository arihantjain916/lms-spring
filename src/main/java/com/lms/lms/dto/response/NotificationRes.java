package com.lms.lms.dto.response;

import com.lms.lms.modals.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class NotificationRes {
    private String id;
    private Notification.Type type;
    private String title;
    private String body;
    private String link;
    private String referenceId;
    private Boolean isRead;
    private Date createdAt;

    public static NotificationRes from(Notification notification) {
        return new NotificationRes(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLink(),
                notification.getReferenceId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
