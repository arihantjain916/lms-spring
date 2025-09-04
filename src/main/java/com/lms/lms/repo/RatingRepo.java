package com.lms.lms.repo;

import com.lms.lms.modals.Ratings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RatingRepo extends JpaRepository<Ratings, String> {
    @Query("SELECT r FROM Ratings r WHERE r.course.id = :courseId")
    List<Ratings> findByCourseId(Long courseId);
}