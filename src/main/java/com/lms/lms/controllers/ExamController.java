package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.ExamReq;
import com.lms.lms.dto.response.CustomCourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.ExamRes;
import com.lms.lms.mappers.ExamMapper;
import com.lms.lms.modals.Exam;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.ExamRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private UserDetails userDetails;
    @Autowired
    private ExamMapper examMapper;

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

    @DeleteMapping("/{examId}/delete")
    public ResponseEntity<Default> deleteExam(@PathVariable String examId) {
        try {
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            examRepo.updateStaus(examId, Exam.Staus.ARCHIVED);
            return ResponseEntity.ok().body(new Default("Course Deleted Successfully", true, null, null));
        } catch (Exception ex) {
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
}
