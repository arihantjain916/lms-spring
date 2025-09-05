package com.lms.lms.repo;

import com.lms.lms.modals.Ratings;
import com.lms.lms.modals.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, String> {

    @Query("SELECT r FROM Review r WHERE r.course.id = :courseId")
    List<Ratings> findByCourseId(Long courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.vote_type = :voteType")
    Integer countReviewByCourseIdAndVoteType(Long courseId, Review.VoteType voteType);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId")
    Integer countReviewByCourseId(Long courseId);


}
