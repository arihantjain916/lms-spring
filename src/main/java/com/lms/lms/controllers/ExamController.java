package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.ExamReq;
import com.lms.lms.dto.request.ExamSubmitReq;
import com.lms.lms.dto.request.QuestionSubmitReq;
import com.lms.lms.dto.response.CustomCourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.ExamRes;
import com.lms.lms.mappers.ExamMapper;
import com.lms.lms.modals.*;
import com.lms.lms.repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private ExamRepo examRepo;

    @Autowired
    private QuestionAttemptRepo questionAttemptRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private ReportCardRepo reportCardRepo;

    @Autowired
    private UserDetails userDetails;
    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private ExamAttemptRepo examAttemptRepo;


    @GetMapping("/{courseId}")
    public ResponseEntity<Default> getAllExams(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "PUBLISHED") String status
    ) {
        try {
            var course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }
            var ExamStatus = Exam.Staus.valueOf(status);
            List<Exam> exams = examRepo.findByCourses_IdAndStatus(courseId, ExamStatus);
            List<ExamRes> list = exams.stream().map(exam -> {
                ExamRes examRes = examMapper.toDto(exam);
                CustomCourseRes courseRes = new CustomCourseRes(exam.getCourses().getId(), exam.getCourses().getTitle(), exam.getCourses().getDescription());
                examRes.setCourse(courseRes);
                return examRes;
            }).toList();
            return ResponseEntity.ok().body(new Default("Exam Fetched Successfully", true, null, list));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/create")
    public ResponseEntity<Default> addExam(@Valid @RequestBody ExamReq examReq) {
        try {
            var user = userDetails.userDetails();
            if (user == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }
            var course = coursesRepo.findById(examReq.getCourseId()).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            Exam exam = new Exam();
            exam.setCourses(course);
            exam.setShuffleQuestions(Boolean.TRUE.equals(examReq.getShuffleQuestions()) || examReq.getShuffleQuestions() == null);
            exam.setShowScoreImmediately(Boolean.TRUE.equals(examReq.getShowScoreImmediately()) || examReq.getShuffleQuestions() == null);
            exam.setMaxAttempts(examReq.getMaxAttempts());
            exam.setTitle(examReq.getTitle());
            exam.setStartsAt(convertStringToInstant(examReq.getStartsAt()));
            exam.setEndsAt(convertStringToInstant(examReq.getEndsAt()));
            exam.setTimeLimitMin(examReq.getTimeLimitMin());
            exam.setStatus(Exam.Staus.DRAFT);
            exam.setPassMarks(examReq.getPassMarks());
            exam.setTotalMarks(examReq.getTotalMarks());
            exam.setUser(user);

            examRepo.save(exam);
            return ResponseEntity.ok().body(new Default("Exam Created Successfully", true, null, null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/{examId}/{status}")
    public ResponseEntity<Default> updateStatus(@PathVariable String examId, @PathVariable String status) {
        try {
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            Exam.Staus ExamStatus = null;
            try {
                ExamStatus = Exam.Staus.valueOf(status.toUpperCase());
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(new Default("Invalid Status. Status Include DRAFT, UnPUBLISHED,PUBLISHED, ARCHIVED", true, null, null));
            }

            examRepo.updateStaus(examId, ExamStatus);
            return ResponseEntity.ok().body(new Default("Status Updated Successfully", true, null, null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    public ResponseEntity<Default> updateExam(@Valid @RequestBody ExamReq examReq) {
        try {
            if (examReq.getId().isBlank()) {
                return ResponseEntity.badRequest().body(new Default("Exam Id is Required", false, null, null));
            }
            var examDetails = examRepo.findById(examReq.getId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            examDetails.setShuffleQuestions(Boolean.TRUE.equals(examReq.getShuffleQuestions()) || examReq.getShuffleQuestions() == null);
            examDetails.setShowScoreImmediately(Boolean.TRUE.equals(examReq.getShowScoreImmediately()) || examReq.getShuffleQuestions() == null);
            examDetails.setMaxAttempts(examReq.getMaxAttempts());
            examDetails.setTitle(examReq.getTitle());
            examDetails.setStartsAt(convertStringToInstant(examReq.getStartsAt()));
            examDetails.setEndsAt(convertStringToInstant(examReq.getEndsAt()));
            examDetails.setTimeLimitMin(examReq.getTimeLimitMin());
            examDetails.setStatus(Exam.Staus.DRAFT);
            examDetails.setPassMarks(examReq.getPassMarks());
            examDetails.setTotalMarks(examReq.getTotalMarks());

            examRepo.save(examDetails);
            return ResponseEntity.ok().body(new Default("Exam Details Updated Successfully", true, null, null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/{examId}/delete")
    @Transactional
    public ResponseEntity<Default> deleteExam(@PathVariable String examId) {
        try {
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            examRepo.updateStaus(examId, Exam.Staus.ARCHIVED);
            return ResponseEntity.ok().body(new Default("Exam Deleted Successfully", true, null, null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

//    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/attempt/{examId}")
    public ResponseEntity<Default> attemptExam(@PathVariable String examId){
        try{
            var user = userDetails.userDetails();
            if (user == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            var isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), examDetails.getCourses().getId());

            if (!isUserEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User is not enrolled in the course", false, null, null));
            }

            List<ExamAttempt> isExamAttempt =examAttemptRepo.findByUser_IdAndExam_Id(user.getId(), examDetails.getId());

            if (isExamAttempt.size() >= examDetails.getMaxAttempts()) {
                return ResponseEntity.badRequest().body(new Default("You have already Attempted the Exam", false, null, null));
            }

            ExamAttempt examAttempt = new ExamAttempt();
            examAttempt.setIsAttempt(Boolean.TRUE);
            examAttempt.setExam(examDetails);
            examAttempt.setUser(examDetails.getUser());
            examAttempt.setIsCompleted(Boolean.FALSE);

            examAttemptRepo.save(examAttempt);

            return ResponseEntity.ok().body(new Default("Exam Attempted Successfully", true, null, null));
        }
        catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    @PutMapping("/submit")
    @Transactional
    public ResponseEntity<Default> submitExam(@Valid @RequestBody ExamSubmitReq examSubmitReq){
        try{
            var examDetails = examRepo.findById(examSubmitReq.getExamId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            var user = userDetails.userDetails();
            if (user == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }
            var isExamAttempt = examAttemptRepo.findByUser_IdAndExam_Id(user.getId(), examSubmitReq.getExamId());
            if (isExamAttempt.isEmpty()) {
                return ResponseEntity.badRequest().body(new Default("User not attempt exam yet.", false, null, null));
            }

            examAttemptRepo.markExamCompete(examSubmitReq.getExamId(), Boolean.TRUE);

            for (QuestionSubmitReq questionSubmitReq: examSubmitReq.getQuestions()){
                var isQuestionExist = questionRepo.findById(questionSubmitReq.getQuestionId()).orElse(null);
                if (isQuestionExist == null) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Question Id", false, null, null));
                }
                QuestionAttempt questionAttempt = new QuestionAttempt();
                questionAttempt.setQuestions(isQuestionExist);
                questionAttempt.setAnswer(questionSubmitReq.getAnswer());
                questionAttempt.setExam(examDetails);
                questionAttempt.setUser(user);
                questionAttemptRepo.save(questionAttempt);
            }

            if(examDetails.isShowScoreImmediately()){
                Boolean isGenerated = generateReportCard(examDetails,user);

                if(!isGenerated){
                    return ResponseEntity.badRequest().body(new Default("Error in generating report card. Contact Support.", false, null, null));
                }
            }

            return ResponseEntity.ok().body(new Default("Exam Submitted Successfully", true, null, null));
        }
        catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }

    public Instant convertStringToInstant(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.of("UTC"));

            LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

            return localDateTime.atZone(ZoneId.of("UTC")).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean generateReportCard(Exam exam, User user) {
        try {
            ReportCard isReportCardGenerated = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), exam.getId());
            if (isReportCardGenerated != null) {
                return Boolean.FALSE;
            }
            List<QuestionAttempt> attempts =
                    questionAttemptRepo.findByUser_IdAndExam_Id(user.getId(), exam.getId());

            BigDecimal totalMarks = BigDecimal.ZERO;
            BigDecimal obtainedMarks = BigDecimal.ZERO;
            String grade = "";

            BigDecimal percentage = BigDecimal.ZERO;

            for (QuestionAttempt attempt : attempts) {
                Questions question = attempt.getQuestions();
                if (question == null) continue;

                totalMarks = totalMarks.add(question.getMarks());

                if (question.getType() == Questions.Type.MCQ) {
                    String correctOptionId = question.getOptions().stream()
                            .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                            .map(QuestionOptions::getId)
                            .findFirst()
                            .orElse(null);

                    if(correctOptionId == null) {
                        continue;
                    }

                    boolean isCorrect = correctOptionId.equals(attempt.getAnswer());

                    if (isCorrect) {
                        obtainedMarks = obtainedMarks.add(question.getMarks());
                    }
                }
                if (question.getType() == Questions.Type.TRUE_FALSE) {
                    boolean isCorrect = Boolean.parseBoolean(attempt.getAnswer()) == Boolean.TRUE.equals(question.getCorrectOption());

                    if (isCorrect) {
                        obtainedMarks = obtainedMarks.add(question.getMarks());
                    }
                }

                 percentage = totalMarks.compareTo(BigDecimal.ZERO) > 0
                        ? obtainedMarks.multiply(BigDecimal.valueOf(100)).divide(totalMarks, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                grade = calculateGrade(percentage);
            }

            ReportCard reportCard = new ReportCard();
            reportCard.setTotalMarks(totalMarks);
            reportCard.setObtainedMarks(obtainedMarks);
            reportCard.setPercentage(percentage);
            reportCard.setGrade(grade);
            reportCard.setExam(exam);
            reportCard.setUser(user);
            reportCardRepo.save(reportCard);

            return Boolean.TRUE;

        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    private String calculateGrade(BigDecimal percentage) {
        if (percentage == null) return "N/A";

        double pct = percentage.doubleValue();

        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }

}
