package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.QuestionReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.QuestionRes;
import com.lms.lms.mappers.QuestionMapper;
import com.lms.lms.modals.Questions;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.QuestionRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController {


    @Autowired
    private ExamRepo examRepo;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private QuestionMapper questionMapper;

    @GetMapping("/{examId}")
    public ResponseEntity<Default> getAllQuestionsByExamId(@PathVariable String examId) {
        try {
            var ExamDetails = examRepo.findById(examId).orElse(null);
            if (ExamDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            List<QuestionRes> questions = questionRepo.findByExam_Id(examId).stream().map(questionMapper::toDto).toList();
            return ResponseEntity.ok().body(new Default("Question Fetched Successfully", true, null, questions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/add")
    public ResponseEntity<Default> addQuestion(@Valid @RequestBody QuestionReq questionReq) {
        try {
            var ExamDetails = examRepo.findById(questionReq.getExamId()).orElse(null);
            if (ExamDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            Questions questions = new Questions();
            questions.setType(questionReq.getType());
            questions.setMarks(questionReq.getMarks());
            questions.setTitle(questionReq.getTitle());
            questions.setDescription(questionReq.getDescription());
            questions.setExam(ExamDetails);
            questionRepo.save(questions);
            return ResponseEntity.ok().body(new Default("Question Created Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    public ResponseEntity<Default> updateQuestion(@Valid @RequestBody QuestionReq questionReq) {
        try {
            if (questionReq.getId() == null || questionReq.getId().isEmpty()) {
                return ResponseEntity.badRequest().body(new Default("Question Id is required", false, null, null));
            }
            var QuestionDetails = questionRepo.findById(questionReq.getId()).orElse(null);
            if (QuestionDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Question Id", false, null, null));
            }

            QuestionDetails.setType(questionReq.getType());
            QuestionDetails.setMarks(questionReq.getMarks());
            QuestionDetails.setTitle(questionReq.getTitle());
            QuestionDetails.setDescription(questionReq.getDescription());
            questionRepo.save(QuestionDetails);

            return ResponseEntity.ok().body(new Default("Question Updated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteQuestion(@PathVariable String id) {
        try {
            var QuestionDetails = questionRepo.findById(id).orElse(null);
            if (QuestionDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            questionRepo.delete(QuestionDetails);

            return ResponseEntity.ok().body(new Default("Question Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }

    }
}
