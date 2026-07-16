package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

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
    // empty for every non-MCQ type; never null, so the client can iterate unconditionally
    private List<StudentResultOptionRes> options;
    private BigDecimal maximumMarks;
    private BigDecimal awardedMarks;
    private String feedback;
}
