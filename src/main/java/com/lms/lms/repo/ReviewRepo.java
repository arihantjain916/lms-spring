package com.lms.lms.repo;

import com.lms.lms.modals.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepo extends JpaRepository<Review, String> {
}
