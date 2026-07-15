package com.lms.lms.dto.response;

import com.lms.lms.modals.ExamAttempt;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class SubmissionSummaryRes {
    private String attemptId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private Date submittedAt;
    private ExamAttempt.GradingStatus gradingStatus;
}
