package com.lms.lms.repo;

import com.lms.lms.modals.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepo extends JpaRepository<Lesson, String> {
    Page<Lesson> findByCourses_Id(Long courseId, Pageable pageable);
}
