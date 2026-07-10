package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LessonProgressRes {
    private String id;
    private String title;
    private String time;
    private String description;
    private String thumbnailUrl;
    private String status;
    private Integer watchedSeconds;
    private Boolean isCompleted;
}
