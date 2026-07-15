package com.lms.lms.repo;

import com.lms.lms.modals.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepo extends JpaRepository<Lesson, String> {
    Page<Lesson> findByCourses_Id(Long courseId, Pageable pageable);

    // published-only listing for public / non-owner viewers (status is stored free-form, so match case-insensitively)
    Page<Lesson> findByCourses_IdAndStatusIgnoreCase(Long courseId, String status, Pageable pageable);

    List<Lesson> findAllByCourses_IdOrderByCreatedAtAsc(Long courseId);

    Integer countByCourses_Id(Long courseId);
}
