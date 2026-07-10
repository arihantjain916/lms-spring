package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.CourseProgressRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LearningCourseRes;
import com.lms.lms.dto.response.LessonProgressRes;
import com.lms.lms.mappers.CourseMapper;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Lesson;
import com.lms.lms.modals.LessonProgress;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.LessonProgressRepo;
import com.lms.lms.repo.LessonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/learning")
public class LearningController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private LessonProgressRepo lessonProgressRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<Default> getLearningCourse(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, course)) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            List<Lesson> lessons = lessonRepo.findAllByCourses_IdOrderByCreatedAtAsc(courseId);
            Map<String, LessonProgress> progressByLesson = lessonProgressRepo.findByUser_IdAndLesson_Courses_Id(user.getId(), courseId)
                    .stream()
                    .collect(Collectors.toMap(progress -> progress.getLesson().getId(), Function.identity()));

            List<LessonProgressRes> lessonList = lessons
                    .stream()
                    .map(lesson -> {
                        LessonProgress progress = progressByLesson.get(lesson.getId());
                        return new LessonProgressRes(
                                lesson.getId(),
                                lesson.getTitle(),
                                lesson.getTime(),
                                lesson.getDescription(),
                                lesson.getThumbnailUrl(),
                                lesson.getStatus(),
                                progress != null ? progress.getWatchedSeconds() : 0,
                                progress != null && progress.getIsCompleted()
                        );
                    })
                    .toList();

            int totalLessons = lessons.size();
            int completedLessons = (int) lessonList.stream().filter(LessonProgressRes::getIsCompleted).count();
            double percent = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;

            CourseProgressRes progressRes = new CourseProgressRes(courseId, totalLessons, completedLessons, percent, totalLessons > 0 && completedLessons == totalLessons);
            LearningCourseRes res = new LearningCourseRes(courseMapper.toDto(course), progressRes, lessonList);
            return ResponseEntity.ok(new Default("Learning Course Fetched Successfully", true, null, res));
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
