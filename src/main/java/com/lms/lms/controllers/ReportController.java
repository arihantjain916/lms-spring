package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.ReportCardReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.ReportCardRes;
import com.lms.lms.dto.response.StudentResultAnswerRes;
import com.lms.lms.dto.response.StudentResultDetailRes;
import com.lms.lms.dto.response.StudentResultOptionRes;
import com.lms.lms.mappers.ReportMapper;
import com.lms.lms.modals.Exam;
import com.lms.lms.modals.ReportCard;
import com.lms.lms.modals.ExamAttempt;
import com.lms.lms.modals.QuestionAttempt;
import com.lms.lms.modals.QuestionOptions;
import com.lms.lms.modals.Questions;
import com.lms.lms.modals.User;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.ReportCardRepo;
import com.lms.lms.repo.ExamAttemptRepo;
import com.lms.lms.repo.QuestionAttemptRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportCardRepo reportCardRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private ExamRepo examRepo;


    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ExamAttemptRepo examAttemptRepo;

    @Autowired
    private QuestionAttemptRepo questionAttemptRepo;


    @GetMapping("/{examId}/get")
    public ResponseEntity<Default> getReportCard(@PathVariable String examId) {
        try {
            var user = userDetails.userDetails();
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            ReportCard reportCard = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), examId);
            if (reportCard == null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Not Generated Yet", false, null, null));
            }
            ReportCardRes res = new ReportCardRes(
                    reportCard.getId(),
                    reportCard.getTotalMarks(),
                    reportCard.getObtainedMarks(),
                    reportCard.getPercentage(),
                    reportCard.getGrade()
            );
            return ResponseEntity.ok().body(new Default("Report Card Generated Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/generate")
    public ResponseEntity<Default> generateReportCard(@Valid @RequestBody ReportCardReq reportCardReq) {
        try {
            var user = userDetails.userDetails();
            var examDetails = examRepo.findById(reportCardReq.getExamId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            if (!this.canManageExam(examDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this exam", false, null, null));
            }

            var isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), examDetails.getCourses().getId());

            if (!isUserEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User is not enrolled in the course", false, null, null));
            }

            ReportCard isReportCardGenerated = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), reportCardReq.getExamId());

            if (isReportCardGenerated != null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Already Generated. Please update the existing Report Card.", false, null, null));
            }

            ReportCard reportCard = new ReportCard();
            reportCard.setUser(user);
            reportCard.setExam(examDetails);
            reportCard.setPercentage(reportCardReq.getPercentage());
            reportCard.setGrade(reportCardReq.getGrade());
            reportCard.setObtainedMarks(reportCardReq.getObtainedMarks());
            reportCard.setTotalMarks(reportCardReq.getTotalMarks());
            reportCardRepo.save(reportCard);
            return ResponseEntity.ok().body(new Default("Report Card Generated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    public ResponseEntity<Default> update(@Valid @RequestBody ReportCardReq reportCardReq) {
        try {
            var user = userDetails.userDetails();

            var examDetails = examRepo.findById(reportCardReq.getExamId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            if (!this.canManageExam(examDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this exam", false, null, null));
            }

            ReportCard isReportCardGenerated = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), reportCardReq.getExamId());

            if (isReportCardGenerated == null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Not Found.", false, null, null));
            }

            isReportCardGenerated.setPercentage(reportCardReq.getPercentage());
            isReportCardGenerated.setGrade(reportCardReq.getGrade());
            isReportCardGenerated.setObtainedMarks(reportCardReq.getObtainedMarks());
            isReportCardGenerated.setTotalMarks(reportCardReq.getTotalMarks());
            reportCardRepo.save(isReportCardGenerated);
            return ResponseEntity.ok().body(new Default("Report Card Updated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Default> getUserReport() {
        try {
            var user = userDetails.userDetails();
            List<ReportCardRes> reportCard = reportCardRepo.findByUser_Id(user.getId()).stream().map(reportMapper::toDto).toList();

            return ResponseEntity.ok().body(new Default("Report Card Fetched Successfully", true, null, reportCard));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    /**
     * Full learner-owned results. Unlike the grading endpoints, this never accepts
     * a student id and can only return report cards belonging to the JWT principal.
     */
    @GetMapping("/me/details")
    public ResponseEntity<Default> getMyDetailedResults() {
        try {
            User user = userDetails.userDetails();
            List<StudentResultDetailRes> results = reportCardRepo.findByUser_Id(user.getId())
                    .stream()
                    .map(report -> toStudentResult(report, user.getId()))
                    .toList();
            return ResponseEntity.ok(new Default("Detailed results fetched successfully", true, null, results));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/me/{reportId}/details")
    public ResponseEntity<Default> getMyDetailedResult(@PathVariable String reportId) {
        try {
            User user = userDetails.userDetails();
            ReportCard report = reportCardRepo.findByIdAndUser_Id(reportId, user.getId()).orElse(null);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Default("Result not found", false, null, null));
            }
            return ResponseEntity.ok(new Default("Detailed result fetched successfully", true, null,
                    toStudentResult(report, user.getId())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/exam/{examId}")
    public ResponseEntity<Default> getReportByCourse(@PathVariable String examId) {
        try {
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            if (!this.canManageExam(examDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to view reports for this exam", false, null, null));
            }

            List<ReportCardRes> reportCard = reportCardRepo.findByExam_Id(examId).stream().map(reportMapper::toDto).toList();

            return ResponseEntity.ok().body(new Default("Report Card Fetched Successfully", true, null, reportCard));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // an instructor may only access reports for exams on their own courses; admins may access any
    private boolean canManageExam(Exam exam) {
        User current = userDetails.userDetails();

        return current != null &&
                (current.getRole() == User.Role.ADMIN ||
                 (exam != null && exam.getCourses() != null && exam.getCourses().getUser() != null
                         && exam.getCourses().getUser().getId().equals(current.getId())));
    }

    private StudentResultDetailRes toStudentResult(ReportCard report, String userId) {
        Exam exam = report.getExam();
        ExamAttempt attempt = examAttemptRepo
                .findFirstByUser_IdAndExam_IdAndIsCompletedTrueOrderByUpdatedAtDesc(userId, exam.getId())
                .orElse(null);

        List<StudentResultAnswerRes> answers = attempt == null
                ? List.of()
                : questionAttemptRepo.findByExamAttempt_IdOrderByAttemptedAtAsc(attempt.getId())
                .stream().map(this::toStudentAnswer).toList();

        ExamAttempt.GradingStatus gradingStatus = attempt == null
                ? ExamAttempt.GradingStatus.FINALIZED
                : effectiveStatus(attempt, answers);

        return new StudentResultDetailRes(
                report.getId(),
                attempt == null ? null : attempt.getId(),
                exam.getId(),
                exam.getTitle(),
                exam.getCourses() == null ? null : exam.getCourses().getId(),
                exam.getCourses() == null ? null : exam.getCourses().getTitle(),
                report.getTotalMarks(),
                report.getObtainedMarks(),
                report.getPercentage(),
                report.getGrade(),
                attempt == null ? report.getCreatedAt() : attempt.getUpdatedAt(),
                gradingStatus,
                attempt == null ? null : attempt.getGradingFeedback(),
                answers
        );
    }

    private StudentResultAnswerRes toStudentAnswer(QuestionAttempt attempt) {
        Questions question = attempt.getQuestions();
        BigDecimal awardedMarks = attempt.getAwardedMarks();
        if (awardedMarks == null && isAutomaticallyGraded(question)) {
            awardedMarks = isCorrect(question, attempt.getAnswer())
                    ? question.getMarks() : BigDecimal.ZERO;
        }

        String feedback = attempt.getFeedback();
        if ((feedback == null || feedback.isBlank()) && isAutomaticallyGraded(question)) {
            feedback = isCorrect(question, attempt.getAnswer()) ? "Correct" : "Incorrect";
        }

        return new StudentResultAnswerRes(
                attempt.getId(),
                question.getId(),
                question.getType().name(),
                question.getTitle(),
                question.getDescription(),
                displayAnswer(question, attempt.getAnswer()),
                correctAnswer(question),
                optionsOf(question, attempt.getAnswer()),
                question.getMarks(),
                awardedMarks,
                feedback
        );
    }

    /**
     * The full option set for an MCQ, flagging which one the learner picked and which is
     * right. Empty for every other type: only MCQ carries options.
     */
    private List<StudentResultOptionRes> optionsOf(Questions question, String answer) {
        if (question.getType() != Questions.Type.MCQ) {
            return List.of();
        }
        return question.getOptions().stream()
                .map(option -> new StudentResultOptionRes(
                        option.getOption(),
                        // answer holds the option id; null when the question was skipped
                        option.getId().equals(answer),
                        Boolean.TRUE.equals(option.getIsCorrect())))
                .toList();
    }

    private ExamAttempt.GradingStatus effectiveStatus(
            ExamAttempt attempt, List<StudentResultAnswerRes> answers) {
        if (attempt.getGradingStatus() == ExamAttempt.GradingStatus.FINALIZED) {
            return ExamAttempt.GradingStatus.FINALIZED;
        }
        boolean allGraded = !answers.isEmpty()
                && answers.stream().allMatch(answer -> answer.getAwardedMarks() != null);
        return allGraded ? ExamAttempt.GradingStatus.FINALIZED
                : (attempt.getGradingStatus() == null
                ? ExamAttempt.GradingStatus.PENDING : attempt.getGradingStatus());
    }

    private boolean isAutomaticallyGraded(Questions question) {
        return question.getType() == Questions.Type.MCQ
                || question.getType() == Questions.Type.TRUE_FALSE;
    }

    private boolean isCorrect(Questions question, String answer) {
        if (answer == null) return false;
        if (question.getType() == Questions.Type.MCQ) {
            return question.getOptions().stream()
                    .anyMatch(option -> option.getId().equals(answer)
                            && Boolean.TRUE.equals(option.getIsCorrect()));
        }
        if (question.getType() == Questions.Type.TRUE_FALSE) {
            return Boolean.parseBoolean(answer) == Boolean.TRUE.equals(question.getCorrectOption());
        }
        return false;
    }

    private String displayAnswer(Questions question, String answer) {
        if (answer == null || question.getType() != Questions.Type.MCQ) return answer;
        return question.getOptions().stream()
                .filter(option -> option.getId().equals(answer))
                .map(QuestionOptions::getOption)
                .findFirst().orElse(answer);
    }

    private String correctAnswer(Questions question) {
        if (question.getType() == Questions.Type.MCQ) {
            return question.getOptions().stream()
                    .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                    .map(QuestionOptions::getOption)
                    .findFirst().orElse(null);
        }
        if (question.getType() == Questions.Type.TRUE_FALSE) {
            return String.valueOf(Boolean.TRUE.equals(question.getCorrectOption()));
        }
        return null;
    }
}
