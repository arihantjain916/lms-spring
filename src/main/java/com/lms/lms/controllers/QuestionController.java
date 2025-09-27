package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.QuestionReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Questions;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.QuestionRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
public class QuestionController {


    @Autowired
    private ExamRepo examRepo;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private UserDetails userDetails;

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/add")
    public ResponseEntity<Default> addQuestion(@Valid @RequestBody QuestionReq questionReq) {
        try {
            var user = userDetails.userDetails();
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
}
