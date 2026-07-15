package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.LessonReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LessonRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.LessonMapper;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Lesson;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.LessonRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/lesson")

public class LessonController {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getByCourseId(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {

        try {
            var course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.internalServerError().body(new Default("Course Not Found", false, null, null));
            }
            int pageNumber = page > 0 ? page - 1 : 0;
            Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, size, sort);

            Page<Lesson> lessons = lessonRepo.findByCourses_Id(courseId, pageable);

            List<LessonRes> blogRes = lessons.
                    stream()
                    .map(lessonMapper::toDto)
                    .toList();

            PaginatedResponse<LessonRes> paginatedResponse = new PaginatedResponse<>(
                    "Lesson Fetched Successfully",
                    true,
                    blogRes,
                    lessons.getNumber() + 1,
                    lessons.getSize(),
                    lessons.getTotalElements(),
                    lessons.getTotalPages()
            );

            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/add")
    public ResponseEntity<Default> addLesson(@Valid @RequestBody LessonReq lessonReq) {
        try {
            var course = coursesRepo.findById(lessonReq.getCourseId()).orElse(null);
            if (course == null) {
                return ResponseEntity.internalServerError().body(new Default("Course Not Found", false, null, null));
            }

            if (!this.canManageCourse(course)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this course", false, null, null));
            }

            Lesson lesson = new Lesson();
            lesson.setTime(lessonReq.getTime());
            lesson.setDescription(lessonReq.getDescription());
            lesson.setTitle(lessonReq.getTitle());
            lesson.setVideoUrl(lessonReq.getVideoUrl());
            lesson.setThumbnailUrl(lessonReq.getThumbnailUrl());
            lesson.setStatus(lessonReq.getStatus());
            lesson.setCourses(course);

            lessonRepo.save(lesson);
            return ResponseEntity.ok().body(new Default("Lesson Added Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    @Transactional
    public ResponseEntity<Default> updateLesson(@Valid @RequestBody LessonReq lessonReq) {
        try {
            if (lessonReq.getId().isBlank()) {
                return ResponseEntity.internalServerError().body(new Default("Lesson Id is required", false, null, null));
            }
            var lesson = lessonRepo.findById(lessonReq.getId()).orElse(null);
            if (lesson == null) {
                return ResponseEntity.internalServerError().body(new Default("Lesson Not Found", false, null, null));
            }

            // ownership is checked against the lesson's current course
            if (!this.canManageCourse(lesson.getCourses())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this course", false, null, null));
            }

            // a lesson may not be moved to a different course
            if (lesson.getCourses() != null && !lesson.getCourses().getId().equals(lessonReq.getCourseId())) {
                return ResponseEntity.badRequest()
                        .body(new Default("A lesson cannot be moved to another course", false, null, null));
            }

            lesson.setDescription(lessonReq.getDescription());
            lesson.setTitle(lessonReq.getTitle());
            lesson.setVideoUrl(lessonReq.getVideoUrl());
            lesson.setTime(lessonReq.getTime());
            lesson.setThumbnailUrl(lessonReq.getThumbnailUrl());
            lesson.setStatus(lessonReq.getStatus());

            lessonRepo.save(lesson);
            return ResponseEntity.ok().body(new Default("Lesson Updated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/delete/{lessonId}")
    @Transactional
    public ResponseEntity<Default> deleteLesson(@PathVariable String lessonId) {
        try {
            var lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return ResponseEntity.internalServerError().body(new Default("Lesson Not Found", false, null, null));
            }

            if (!this.canManageCourse(lesson.getCourses())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Default("You are not authorized to manage this course", false, null, null));
            }

            lessonRepo.delete(lesson);
            return ResponseEntity.ok().body(new Default("Lesson Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // an instructor may only manage lessons of their own courses; admins may manage any
    private boolean canManageCourse(Courses course) {
        User current = userDetails.userDetails();

        return current != null &&
                (current.getRole() == User.Role.ADMIN ||
                 (course != null && course.getUser() != null && course.getUser().getId().equals(current.getId())));
    }
}
