package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.FinalizeGradingReq;
import com.lms.lms.dto.request.GradeAnswerReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.SubmissionDetailRes;
import com.lms.lms.dto.response.SubmissionSummaryRes;
import com.lms.lms.dto.response.SubmittedAnswerRes;
import com.lms.lms.modals.Exam;
import com.lms.lms.modals.ExamAttempt;
import com.lms.lms.modals.QuestionAttempt;
import com.lms.lms.modals.ReportCard;
import com.lms.lms.modals.User;
import com.lms.lms.repo.ExamAttemptRepo;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.QuestionAttemptRepo;
import com.lms.lms.repo.ReportCardRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/grading")
@PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
public class GradingController {

    private final ExamRepo examRepo;
    private final ExamAttemptRepo examAttemptRepo;
    private final QuestionAttemptRepo questionAttemptRepo;
    private final ReportCardRepo reportCardRepo;
    private final UserDetails userDetails;

    public GradingController(ExamRepo examRepo,
                             ExamAttemptRepo examAttemptRepo,
                             QuestionAttemptRepo questionAttemptRepo,
                             ReportCardRepo reportCardRepo,
                             UserDetails userDetails) {
        this.examRepo = examRepo;
        this.examAttemptRepo = examAttemptRepo;
        this.questionAttemptRepo = questionAttemptRepo;
        this.reportCardRepo = reportCardRepo;
        this.userDetails = userDetails;
    }

    @GetMapping("/exams/{examId}/submissions")
    public ResponseEntity<Default> listSubmissions(@PathVariable String examId) {
        Exam exam = examRepo.findById(examId).orElse(null);
        ResponseEntity<Default> accessError = validateAccess(exam);
        if (accessError != null) return accessError;

        List<SubmissionSummaryRes> submissions = examAttemptRepo
                .findByExam_IdAndIsCompletedTrueOrderByCreatedAtDesc(examId)
                .stream()
                .map(attempt -> new SubmissionSummaryRes(
                        attempt.getId(),
                        attempt.getUser().getId(),
                        attempt.getUser().getName(),
                        attempt.getUser().getEmail(),
                        attempt.getUpdatedAt(),
                        statusOf(attempt)))
                .toList();

        return ResponseEntity.ok(new Default("Submissions fetched successfully", true, null, submissions));
    }

    @GetMapping("/submissions/{attemptId}")
    public ResponseEntity<Default> getSubmission(@PathVariable String attemptId) {
        ExamAttempt attempt = examAttemptRepo.findByIdAndIsCompletedTrue(attemptId).orElse(null);
        if (attempt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Default("Submission not found", false, null, null));
        }
        ResponseEntity<Default> accessError = validateAccess(attempt.getExam());
        if (accessError != null) return accessError;

        return ResponseEntity.ok(new Default(
                "Submission fetched successfully", true, null, toDetail(attempt)));
    }

    @PutMapping("/submissions/{attemptId}/answers/{questionAttemptId}")
    @Transactional
    public ResponseEntity<Default> gradeAnswer(@PathVariable String attemptId,
                                               @PathVariable String questionAttemptId,
                                               @Valid @RequestBody GradeAnswerReq request) {
        ExamAttempt attempt = examAttemptRepo.findByIdAndIsCompletedTrue(attemptId).orElse(null);
        if (attempt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Default("Submission not found", false, null, null));
        }
        ResponseEntity<Default> accessError = validateAccess(attempt.getExam());
        if (accessError != null) return accessError;
        if (statusOf(attempt) == ExamAttempt.GradingStatus.FINALIZED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Default("Finalized grading cannot be changed", false, null, null));
        }

        QuestionAttempt answer = questionAttemptRepo.findById(questionAttemptId).orElse(null);
        if (answer == null || answer.getExamAttempt() == null
                || !attemptId.equals(answer.getExamAttempt().getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Default("Submitted answer not found", false, null, null));
        }
        if (request.getAwardedMarks().compareTo(answer.getQuestions().getMarks()) > 0) {
            return ResponseEntity.badRequest().body(new Default(
                    "Awarded marks cannot exceed " + answer.getQuestions().getMarks(), false, null, null));
        }

        answer.setAwardedMarks(request.getAwardedMarks());
        answer.setFeedback(request.getFeedback());
        questionAttemptRepo.save(answer);
        attempt.setGradingStatus(ExamAttempt.GradingStatus.IN_PROGRESS);
        examAttemptRepo.save(attempt);

        return ResponseEntity.ok(new Default("Answer graded successfully", true, null, toAnswer(answer)));
    }

    @PostMapping("/submissions/{attemptId}/finalize")
    @Transactional
    public ResponseEntity<Default> finalizeGrading(@PathVariable String attemptId,
                                                   @RequestBody(required = false) FinalizeGradingReq request) {
        ExamAttempt attempt = examAttemptRepo.findByIdAndIsCompletedTrue(attemptId).orElse(null);
        if (attempt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Default("Submission not found", false, null, null));
        }
        ResponseEntity<Default> accessError = validateAccess(attempt.getExam());
        if (accessError != null) return accessError;
        if (statusOf(attempt) == ExamAttempt.GradingStatus.FINALIZED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Default("Grading is already finalized", false, null, null));
        }

        List<QuestionAttempt> answers = questionAttemptRepo
                .findByExamAttempt_IdOrderByAttemptedAtAsc(attemptId);
        List<String> ungradedAnswerIds = answers.stream()
                .filter(answer -> answer.getAwardedMarks() == null)
                .map(QuestionAttempt::getId)
                .toList();
        if (!ungradedAnswerIds.isEmpty()) {
            return ResponseEntity.badRequest().body(new Default(
                    "Every submitted answer must be graded before finalization",
                    false, null, ungradedAnswerIds));
        }

        BigDecimal obtainedMarks = answers.stream()
                .map(QuestionAttempt::getAwardedMarks)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMarks = BigDecimal.valueOf(attempt.getExam().getTotalMarks());
        BigDecimal percentage = totalMarks.signum() == 0
                ? BigDecimal.ZERO
                : obtainedMarks.multiply(BigDecimal.valueOf(100))
                .divide(totalMarks, 2, RoundingMode.HALF_UP);

        ReportCard reportCard = reportCardRepo.findByUser_IdAndExam_Id(
                attempt.getUser().getId(), attempt.getExam().getId());
        if (reportCard == null) {
            reportCard = new ReportCard();
            reportCard.setUser(attempt.getUser());
            reportCard.setExam(attempt.getExam());
        }
        reportCard.setObtainedMarks(obtainedMarks);
        reportCard.setTotalMarks(totalMarks);
        reportCard.setPercentage(percentage);
        reportCard.setGrade(calculateGrade(percentage));
        reportCardRepo.save(reportCard);

        attempt.setGradingStatus(ExamAttempt.GradingStatus.FINALIZED);
        attempt.setGradingFeedback(request == null ? null : request.getFeedback());
        attempt.setGradedAt(new Date());
        attempt.setGradedBy(userDetails.userDetails());
        examAttemptRepo.save(attempt);

        return ResponseEntity.ok(new Default("Grading finalized successfully", true, null, toDetail(attempt)));
    }

    private SubmissionDetailRes toDetail(ExamAttempt attempt) {
        List<SubmittedAnswerRes> answers = questionAttemptRepo
                .findByExamAttempt_IdOrderByAttemptedAtAsc(attempt.getId())
                .stream().map(this::toAnswer).toList();
        return new SubmissionDetailRes(
                attempt.getId(), attempt.getExam().getId(), attempt.getExam().getTitle(),
                attempt.getUser().getId(), attempt.getUser().getName(), attempt.getUpdatedAt(),
                statusOf(attempt), attempt.getGradingFeedback(), answers);
    }

    private SubmittedAnswerRes toAnswer(QuestionAttempt answer) {
        return new SubmittedAnswerRes(
                answer.getId(), answer.getQuestions().getId(), answer.getQuestions().getType().name(),
                answer.getQuestions().getTitle(), answer.getAnswer(), answer.getQuestions().getMarks(),
                answer.getAwardedMarks(), answer.getFeedback());
    }

    private ExamAttempt.GradingStatus statusOf(ExamAttempt attempt) {
        return attempt.getGradingStatus() == null
                ? ExamAttempt.GradingStatus.PENDING : attempt.getGradingStatus();
    }

    private ResponseEntity<Default> validateAccess(Exam exam) {
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Default("Exam not found", false, null, null));
        }
        User current = userDetails.userDetails();
        boolean allowed = current != null && (current.getRole() == User.Role.ADMIN
                || (exam.getCourses() != null && exam.getCourses().getUser() != null
                && current.getId().equals(exam.getCourses().getUser().getId())));
        return allowed ? null : ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Default("You are not authorized to grade this exam", false, null, null));
    }

    private String calculateGrade(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(90)) >= 0) return "A+";
        if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) return "A";
        if (percentage.compareTo(BigDecimal.valueOf(70)) >= 0) return "B";
        if (percentage.compareTo(BigDecimal.valueOf(60)) >= 0) return "C";
        if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0) return "D";
        return "F";
    }
}
