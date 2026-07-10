package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseProgressRes {
    private Long courseId;
    private Integer totalLessons;
    private Integer completedLessons;
    private Double progressPercent;
    private Boolean isCompleted;
}
