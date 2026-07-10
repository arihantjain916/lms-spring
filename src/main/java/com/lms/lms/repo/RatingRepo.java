package com.lms.lms.repo;

import com.lms.lms.modals.Ratings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RatingRepo extends JpaRepository<Ratings, String> {

    Page<Ratings> findAllByCourseId(Long courseId, Pageable pageable);
    @Query("SELECT r FROM Ratings r WHERE r.course.id = :courseId")
    List<Ratings> findByCourseId(Long courseId);

    @Query("SELECT AVG(r.rating) FROM Ratings r WHERE r.course.id = :courseId")
    Double avgRatingOfCourse(Long courseId);

    @Query("SELECT COUNT(r) FROM Ratings r WHERE r.course.id = :courseId")
    Integer totalRatingofCourse(Long courseId);

    Boolean existsByCourseIdAndUserId(Long courseId, String userId);
}