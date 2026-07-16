package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.CourseQuestionReq;
import com.lms.lms.dto.response.CourseQuestionRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.QuestionReplyRes;
import com.lms.lms.dto.response.UserRes;
import com.lms.lms.modals.CourseQuestion;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Notification;
import com.lms.lms.modals.QuestionHelpful;
import com.lms.lms.modals.QuestionReply;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CourseQuestionRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.QuestionHelpfulRepo;
import com.lms.lms.repo.QuestionReplyRepo;
import com.lms.lms.service.NotificationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourseQuestionController {

    @Autowired
    private CourseQuestionRepo courseQuestionRepo;

    @Autowired
    private QuestionReplyRepo questionReplyRepo;

    @Autowired
    private QuestionHelpfulRepo questionHelpfulRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private NotificationService notificationService;

    private static String preview(String content) {
        return content.length() > 120 ? content.substring(0, 120) + "..." : content;
    }

    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<?> getCourseQuestions(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            int pageNumber = page > 0 ? page - 1 : 0;

            Page<CourseQuestion> questions;
            if ("helpful".equals(sort)) {
                questions = courseQuestionRepo.findByCourseIdOrderByHelpful(courseId, PageRequest.of(pageNumber, limit));
            } else {
                Sort sortSpec = "oldest".equals(sort) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
                questions = courseQuestionRepo.findByCourse_Id(courseId, PageRequest.of(pageNumber, limit, sortSpec));
            }

            List<CourseQuestionRes> questionList = questions
                    .stream()
                    .map(this::toQuestionRes)
                    .toList();

            PaginatedResponse<CourseQuestionRes> paginatedResponse = new PaginatedResponse<>(
                    "Questions Fetched Successfully",
                    true,
                    questionList,
                    questions.getNumber() + 1,
                    questions.getSize(),
                    questions.getTotalElements(),
                    questions.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/courses/{courseId}/questions")
    public ResponseEntity<Default> addCourseQuestion(@PathVariable Long courseId, @Valid @RequestBody CourseQuestionReq req) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, course)) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            CourseQuestion question = new CourseQuestion();
            question.setContent(req.getContent());
            question.setCourse(course);
            question.setUser(user);
            courseQuestionRepo.save(question);

            // the instructor owns the course; no notification when they ask on their own course
            User instructor = course.getUser();
            if (instructor != null && !instructor.getId().equals(user.getId())) {
                notificationService.notify(
                        instructor,
                        Notification.Type.COURSE_QUESTION,
                        "New question in " + course.getTitle(),
                        user.getName() + " asked: " + preview(question.getContent()),
                        "/courses/" + course.getId() + "/questions/" + question.getId(),
                        question.getId());
            }

            return ResponseEntity.ok(new Default("Question Added Successfully", true, null, this.toQuestionRes(question)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/questions/{questionId}/replies")
    public ResponseEntity<Default> getQuestionReplies(@PathVariable String questionId) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            List<QuestionReplyRes> replies = questionReplyRepo.findByQuestion_IdOrderByCreatedAtAsc(questionId)
                    .stream()
                    .map(reply -> new QuestionReplyRes(
                            reply.getId(),
                            reply.getContent(),
                            new UserRes(reply.getUser().getId(), reply.getUser().getUsername(), reply.getUser().getName()),
                            reply.getCreatedAt()
                    ))
                    .toList();
            return ResponseEntity.ok(new Default("Replies Fetched Successfully", true, null, replies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/questions/{questionId}/replies")
    public ResponseEntity<Default> addQuestionReply(@PathVariable String questionId, @Valid @RequestBody CourseQuestionReq req) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, question.getCourse())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            QuestionReply reply = new QuestionReply();
            reply.setContent(req.getContent());
            reply.setQuestion(question);
            reply.setUser(user);
            questionReplyRepo.save(reply);

            // whoever asked wants to know it was answered; skip when they reply to themselves
            User asker = question.getUser();
            if (asker != null && !asker.getId().equals(user.getId())) {
                notificationService.notify(
                        asker,
                        Notification.Type.COURSE_QUESTION,
                        user.getName() + " replied to your question",
                        preview(reply.getContent()),
                        "/courses/" + question.getCourse().getId() + "/questions/" + question.getId(),
                        question.getId());
            }

            QuestionReplyRes res = new QuestionReplyRes(
                    reply.getId(),
                    reply.getContent(),
                    new UserRes(user.getId(), user.getUsername(), user.getName()),
                    reply.getCreatedAt()
            );
            return ResponseEntity.ok(new Default("Reply Added Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/questions/{questionId}/helpful")
    public ResponseEntity<Default> markQuestionHelpful(@PathVariable String questionId) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, question.getCourse())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            Boolean isAlreadyMarked = questionHelpfulRepo.existsByQuestion_IdAndUser_Id(questionId, user.getId());
            if (isAlreadyMarked) {
                return ResponseEntity.badRequest().body(new Default("Question Already Marked As Helpful", false, null, null));
            }

            QuestionHelpful helpful = new QuestionHelpful();
            helpful.setQuestion(question);
            helpful.setUser(user);
            questionHelpfulRepo.save(helpful);

            return ResponseEntity.ok(new Default("Question Marked As Helpful Successfully", true, null, questionHelpfulRepo.countByQuestion_Id(questionId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/questions/{questionId}/helpful")
    @Transactional
    public ResponseEntity<Default> unmarkQuestionHelpful(@PathVariable String questionId) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isMarked = questionHelpfulRepo.existsByQuestion_IdAndUser_Id(questionId, user.getId());
            if (!isMarked) {
                return ResponseEntity.badRequest().body(new Default("Question Is Not Marked As Helpful", false, null, null));
            }

            questionHelpfulRepo.deleteByQuestion_IdAndUser_Id(questionId, user.getId());
            return ResponseEntity.ok(new Default("Helpful Mark Removed Successfully", true, null, questionHelpfulRepo.countByQuestion_Id(questionId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PatchMapping("/questions/{questionId}")
    public ResponseEntity<Default> updateQuestion(@PathVariable String questionId, @Valid @RequestBody CourseQuestionReq req) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            boolean isOwner = question.getUser().getId().equals(user.getId());
            if (!isOwner && user.getRole() != User.Role.ADMIN) {
                return new ResponseEntity<>(new Default("Only The Question Author Can Update It", false, null, null), HttpStatus.FORBIDDEN);
            }

            question.setContent(req.getContent());
            courseQuestionRepo.save(question);

            return ResponseEntity.ok(new Default("Question Updated Successfully", true, null, this.toQuestionRes(question)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/questions/{questionId}")
    @Transactional
    public ResponseEntity<Default> deleteQuestion(@PathVariable String questionId) {
        try {
            CourseQuestion question = courseQuestionRepo.findById(questionId).orElse(null);
            if (question == null) {
                return new ResponseEntity<>(new Default("Question Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            boolean isOwner = question.getUser().getId().equals(user.getId());
            boolean isCourseInstructor = question.getCourse().getUser() != null && question.getCourse().getUser().getId().equals(user.getId());
            if (!isOwner && !isCourseInstructor && user.getRole() != User.Role.ADMIN) {
                return new ResponseEntity<>(new Default("Only The Question Author Can Delete It", false, null, null), HttpStatus.FORBIDDEN);
            }

            questionHelpfulRepo.deleteByQuestion_Id(questionId);
            questionReplyRepo.deleteByQuestion_Id(questionId);
            courseQuestionRepo.delete(question);

            return ResponseEntity.ok(new Default("Question Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private CourseQuestionRes toQuestionRes(CourseQuestion question) {
        return new CourseQuestionRes(
                question.getId(),
                question.getContent(),
                new UserRes(question.getUser().getId(), question.getUser().getUsername(), question.getUser().getName()),
                questionReplyRepo.countByQuestion_Id(question.getId()),
                questionHelpfulRepo.countByQuestion_Id(question.getId()),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    private boolean canAccessCourse(User user, Courses course) {
        if (user == null || user.getIsDeleted()) {
            return false;
        }
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (course.getUser() != null && course.getUser().getId().equals(user.getId())) {
            return true;
        }
        return enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), course.getId());
    }
}
