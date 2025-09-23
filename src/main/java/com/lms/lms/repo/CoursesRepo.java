package com.lms.lms.repo;

import com.lms.lms.modals.Courses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoursesRepo extends JpaRepository<Courses , Long> {

    Optional<Courses> findBySlug(String slug);

    Page<Courses> findByCategoryId(String categoryId, Pageable pageable);

    List<Courses> findAllByUserId(String userId);
}
