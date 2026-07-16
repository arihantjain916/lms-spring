package com.lms.lms.dto.response;

import com.lms.lms.modals.ExamAttempt;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class StudentResultDetailRes {
    private String reportId;
    private String attemptId;
    private String examId;
    private String examTitle;
    private Long courseId;
    private String courseTitle;
    private BigDecimal totalMarks;
    private BigDecimal obtainedMarks;
    private BigDecimal percentage;
    private String grade;
    private Date submittedAt;
    private ExamAttempt.GradingStatus gradingStatus;
    private String feedback;
    private List<StudentResultAnswerRes> answers;
}
