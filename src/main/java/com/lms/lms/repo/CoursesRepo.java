package com.lms.lms.repo;

import com.lms.lms.modals.Courses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoursesRepo extends JpaRepository<Courses , Long> {

    Optional<Courses> findBySlug(String slug);
    List<Courses> findByCategoryId(String categoryId);
    List<Courses> findAllBySlug(String slug);

    List<Courses> findAllByUserId(String userId);
}
