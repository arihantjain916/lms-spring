package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.LessonResourceReq;
import com.lms.lms.dto.request.ProgressReq;
import com.lms.lms.dto.response.CourseProgressRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LessonRes;
import com.lms.lms.dto.response.LessonResourceRes;
import com.lms.lms.dto.response.PlaybackRes;
import com.lms.lms.mappers.LessonMapper;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Lesson;
import com.lms.lms.modals.LessonProgress;
import com.lms.lms.modals.LessonResource;
import com.lms.lms.modals.User;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.LessonProgressRepo;
import com.lms.lms.repo.LessonRepo;
import com.lms.lms.repo.LessonResourceRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonsController {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private LessonProgressRepo lessonProgressRepo;

    @Autowired
    private LessonResourceRepo lessonResourceRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/{lessonId}")
    public ResponseEntity<Default> getLessonById(@PathVariable String lessonId) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, lesson.getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            LessonRes res = lessonMapper.toDto(lesson);
            // the video url is only exposed through the playback endpoint
            res.setVideoUrl(null);
            return ResponseEntity.ok(new Default("Lesson Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{lessonId}/playback")
    public ResponseEntity<Default> getLessonPlayback(@PathVariable String lessonId) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, lesson.getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            LessonProgress progress = lessonProgressRepo.findByUser_IdAndLesson_Id(user.getId(), lessonId).orElse(null);
            PlaybackRes res = new PlaybackRes(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getVideoUrl(),
                    progress != null ? progress.getWatchedSeconds() : 0,
                    progress != null ? progress.getIsCompleted() : false
            );
            return ResponseEntity.ok(new Default("Playback Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PatchMapping("/{lessonId}/progress")
    public ResponseEntity<Default> updateLessonProgress(@PathVariable String lessonId, @Valid @RequestBody ProgressReq req) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, lesson.getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            LessonProgress progress = lessonProgressRepo.findByUser_IdAndLesson_Id(user.getId(), lessonId).orElse(null);
            if (progress == null) {
                progress = new LessonProgress();
                progress.setLesson(lesson);
                progress.setUser(user);
            }
            progress.setWatchedSeconds(req.getWatchedSeconds());
            lessonProgressRepo.save(progress);

            PlaybackRes res = new PlaybackRes(lesson.getId(), lesson.getTitle(), null, progress.getWatchedSeconds(), progress.getIsCompleted());
            return ResponseEntity.ok(new Default("Progress Updated Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<Default> completeLesson(@PathVariable String lessonId) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, lesson.getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            LessonProgress progress = lessonProgressRepo.findByUser_IdAndLesson_Id(user.getId(), lessonId).orElse(null);
            if (progress == null) {
                progress = new LessonProgress();
                progress.setLesson(lesson);
                progress.setUser(user);
            }
            if (!progress.getIsCompleted()) {
                progress.setIsCompleted(true);
                progress.setCompletedAt(new Date());
            }
            lessonProgressRepo.save(progress);

            Long courseId = lesson.getCourses().getId();
            Integer totalLessons = lessonRepo.countByCourses_Id(courseId);
            Integer completedLessons = lessonProgressRepo.countByUser_IdAndLesson_Courses_IdAndIsCompletedTrue(user.getId(), courseId);
            double percent = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;

            CourseProgressRes res = new CourseProgressRes(courseId, totalLessons, completedLessons, percent, totalLessons > 0 && completedLessons.equals(totalLessons));
            return ResponseEntity.ok(new Default("Lesson Completed Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{lessonId}/resources")
    public ResponseEntity<Default> getLessonResources(@PathVariable String lessonId) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, lesson.getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            List<LessonResourceRes> resources = lessonResourceRepo.findByLesson_IdOrderByCreatedAtAsc(lessonId)
                    .stream()
                    .map(resource -> new LessonResourceRes(resource.getId(), resource.getTitle(), resource.getType(), resource.getCreatedAt()))
                    .toList();
            return ResponseEntity.ok(new Default("Resources Fetched Successfully", true, null, resources));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/{lessonId}/resources")
    public ResponseEntity<Default> addLessonResource(@PathVariable String lessonId, @Valid @RequestBody LessonResourceReq req) {
        try {
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                return new ResponseEntity<>(new Default("Lesson Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            boolean isCourseOwner = lesson.getCourses().getUser() != null && lesson.getCourses().getUser().getId().equals(user.getId());
            if (user.getRole() != User.Role.ADMIN && !isCourseOwner) {
                return new ResponseEntity<>(new Default("Only The Course Instructor Can Add Resources", false, null, null), HttpStatus.FORBIDDEN);
            }

            LessonResource resource = new LessonResource();
            resource.setTitle(req.getTitle());
            resource.setUrl(req.getUrl());
            resource.setType(req.getType());
            resource.setLesson(lesson);
            lessonResourceRepo.save(resource);

            return ResponseEntity.ok(new Default("Resource Added Successfully", true, null, new LessonResourceRes(resource.getId(), resource.getTitle(), resource.getType(), resource.getCreatedAt())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
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
