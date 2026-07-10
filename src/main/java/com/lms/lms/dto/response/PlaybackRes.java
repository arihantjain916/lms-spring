package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaybackRes {
    private String lessonId;
    private String title;
    private String videoUrl;
    private Integer watchedSeconds;
    private Boolean isCompleted;
}
