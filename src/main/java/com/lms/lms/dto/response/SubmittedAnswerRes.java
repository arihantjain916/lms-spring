package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SubmittedAnswerRes {
    private String questionAttemptId;
    private String questionId;
    private String questionType;
    private String title;
    // human-readable: the option text for MCQ, the raw response otherwise
    private String answer;
    // the stored option id for MCQ, null for every other type — kept so the grading UI
    // can match the pick back to an option without string-comparing labels
    private String answerOptionId;
    private BigDecimal maximumMarks;
    private BigDecimal awardedMarks;
    private String feedback;
}
