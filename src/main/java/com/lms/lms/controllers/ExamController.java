package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Exam;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.ExamRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private ExamRepo examRepo;

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
            return ResponseEntity.ok().body(new Default("Exam Fetched Successfully", true, null, exams));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new Default(ex.getMessage(), false, null, null));
        }
    }
}
