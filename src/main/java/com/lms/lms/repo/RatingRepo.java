package com.lms.lms.repo;

import com.lms.lms.modals.Ratings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepo extends JpaRepository<Ratings, String> {
}
