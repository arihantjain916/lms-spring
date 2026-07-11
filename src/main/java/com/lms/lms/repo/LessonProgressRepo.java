package com.lms.lms.repo;

import com.lms.lms.modals.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepo extends JpaRepository<LessonProgress, String> {

    Optional<LessonProgress> findByUser_IdAndLesson_Id(String userId, String lessonId);

    List<LessonProgress> findByUser_IdAndLesson_Courses_Id(String userId, Long courseId);

    Integer countByUser_IdAndLesson_Courses_IdAndIsCompletedTrue(String userId, Long courseId);

    int deleteByUser_Id(String userId);
}
