package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.OptionReq;
import com.lms.lms.dto.request.QuestionReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.QuestionRes;
import com.lms.lms.mappers.QuestionMapper;
import com.lms.lms.modals.Exam;
import com.lms.lms.modals.QuestionOptions;
import com.lms.lms.modals.Questions;
import com.lms.lms.modals.User;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.QuestionOptionRepo;
import com.lms.lms.repo.QuestionRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private QuestionOptionRepo questionOptionRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

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

            // owners/admins may read questions for any exam (including drafts);
            // everyone else may only read PUBLISHED exams they are enrolled in
            if (!this.canManageExam(ExamDetails)) {
                var user = userDetails.userDetails();
                boolean published = ExamDetails.getStatus() == Exam.Staus.PUBLISHED;
                boolean enrolled = user != null && enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), ExamDetails.getCourses().getId());
                if (!published || !enrolled) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new Default("You are not authorized to view these questions", false, null, null));
                }
            }

            List<QuestionRes> questions = questionRepo.findByExam_Id(examId).stream().map(questionMapper::toDto).toList();
            return ResponseEntity.ok().body(new Default("Question Fetched Successfully", true, null, questions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<Default> addQuestion(@Valid @RequestBody QuestionReq questionReq) {
        try {
            var ExamDetails = examRepo.findById(questionReq.getExamId()).orElse(null);
            if (ExamDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            if (!this.canManageExam(ExamDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this exam", false, null, null));
            }

            if (questionReq.getType() == Questions.Type.MCQ) {
                if (questionReq.getOptions() == null || questionReq.getOptions().isEmpty()) {
                    return ResponseEntity.badRequest().body(new Default("Options are required for MCQ", false, null, null));
                }
                boolean hasCorrect = questionReq.getOptions().stream().anyMatch(o -> Boolean.TRUE.equals(o.getIsCorrect()));
                if (!hasCorrect) {
                    return ResponseEntity.badRequest().body(new Default("At least one option must be correct", false, null, null));
                }
            }
            Questions questions = new Questions();
            questions.setType(questionReq.getType());
            questions.setMarks(questionReq.getMarks());
            questions.setTitle(questionReq.getTitle());
            questions.setDescription(questionReq.getDescription());
            questions.setExam(ExamDetails);
            questions.setCorrectOption(Boolean.TRUE.equals(questionReq.getCorrectOption()));
            questionRepo.save(questions);

            if(questionReq.getType() == Questions.Type.MCQ){
                for(OptionReq option: questionReq.getOptions()){
                    QuestionOptions option1 = new QuestionOptions();
                    option1.setOption(option.getOption());
                    option1.setIsCorrect(option.getIsCorrect());
                    option1.setQuestions(questions);
                    questionOptionRepo.save(option1);
                }
            }
            return ResponseEntity.ok().body(new Default("Question Created Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    @Transactional
    public ResponseEntity<Default> updateQuestion(@Valid @RequestBody QuestionReq questionReq) {
        try {
            if (questionReq.getId() == null || questionReq.getId().isEmpty()) {
                return ResponseEntity.badRequest().body(new Default("Question Id is required", false, null, null));
            }
            var QuestionDetails = questionRepo.findById(questionReq.getId()).orElse(null);
            if (QuestionDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Question Id", false, null, null));
            }

            if (!this.canManageExam(QuestionDetails.getExam())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this exam", false, null, null));
            }

            if (questionReq.getType() == Questions.Type.MCQ) {
                if (questionReq.getOptions() == null || questionReq.getOptions().isEmpty()) {
                    return ResponseEntity.badRequest().body(new Default("Options are required for MCQ", false, null, null));
                }
                boolean hasCorrect = questionReq.getOptions().stream().anyMatch(o -> Boolean.TRUE.equals(o.getIsCorrect()));
                if (!hasCorrect) {
                    return ResponseEntity.badRequest().body(new Default("At least one option must be correct", false, null, null));
                }
            }

            QuestionDetails.setType(questionReq.getType());
            QuestionDetails.setMarks(questionReq.getMarks());
            QuestionDetails.setTitle(questionReq.getTitle());
            QuestionDetails.setDescription(questionReq.getDescription());
            QuestionDetails.setCorrectOption(Boolean.TRUE.equals(questionReq.getCorrectOption()));

            // rebuild options: orphanRemoval clears the old ones, then recreate from the request
            QuestionDetails.getOptions().clear();
            if (questionReq.getType() == Questions.Type.MCQ) {
                for (OptionReq option : questionReq.getOptions()) {
                    QuestionOptions option1 = new QuestionOptions();
                    option1.setOption(option.getOption());
                    option1.setIsCorrect(option.getIsCorrect());
                    option1.setQuestions(QuestionDetails);
                    QuestionDetails.getOptions().add(option1);
                }
            }
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

            if (!this.canManageExam(QuestionDetails.getExam())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this exam", false, null, null));
            }

            questionRepo.delete(QuestionDetails);

            return ResponseEntity.ok().body(new Default("Question Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }

    }

    // an instructor may only manage questions of exams on their own courses; admins may manage any
    private boolean canManageExam(Exam exam) {
        User current = userDetails.userDetails();

        return current != null &&
                (current.getRole() == User.Role.ADMIN ||
                 (exam != null && exam.getCourses() != null && exam.getCourses().getUser() != null
                         && exam.getCourses().getUser().getId().equals(current.getId())));
    }
}
