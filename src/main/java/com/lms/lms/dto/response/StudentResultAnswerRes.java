package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StudentResultAnswerRes {
    private String questionAttemptId;
    private String questionId;
    private String questionType;
    private String title;
    private String description;
    private String answer;
    private String correctAnswer;
    private BigDecimal maximumMarks;
    private BigDecimal awardedMarks;
    private String feedback;
}
