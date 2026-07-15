package com.lms.lms.dto.response;

import com.lms.lms.modals.ExamAttempt;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class SubmissionDetailRes {
    private String attemptId;
    private String examId;
    private String examTitle;
    private String studentId;
    private String studentName;
    private Date submittedAt;
    private ExamAttempt.GradingStatus gradingStatus;
    private String feedback;
    private List<SubmittedAnswerRes> answers;
}
