package com.lms.lms.repo;

import com.lms.lms.modals.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepo extends JpaRepository<Lesson, String> {
    Page<Lesson> findByCourses_Id(Long courseId, Pageable pageable);

    List<Lesson> findAllByCourses_IdOrderByCreatedAtAsc(Long courseId);

    Integer countByCourses_Id(Long courseId);
}
