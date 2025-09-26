package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExamReq {
    private String id;

    private Boolean shuffleQuestions;
    private Boolean showScoreImmediately;

    private Integer maxAttempts;

    @NotNull(message = "Exam title is required")
    private String title;

    @NotNull(message = "Exam start time is required")
    private String startsAt;

    @NotNull(message = "Exam end time is required")
    private String endsAt;

    @NotNull(message = "Exam time limit is required")
    private Integer timeLimitMin;

    @NotNull(message = "Course id is required")
    private Long courseId;
}
