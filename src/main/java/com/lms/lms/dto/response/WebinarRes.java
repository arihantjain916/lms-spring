package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class WebinarRes {
    private String id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private Date scheduledAt;
    private Integer durationMinutes;
    private String status;
    private String categoryId;
    private String categoryName;
    private UserRes host;
    private Integer registrationsCount;
    private Boolean hasRecording;
}
