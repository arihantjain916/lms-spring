package com.lms.lms.repo;

import com.lms.lms.modals.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepo extends JpaRepository<Lesson, String> {
    List<Lesson> findByCourses_Id(Long courseId);
}
