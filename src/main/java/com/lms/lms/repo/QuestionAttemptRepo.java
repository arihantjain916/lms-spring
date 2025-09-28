package com.lms.lms.repo;

import com.lms.lms.modals.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAttemptRepo extends JpaRepository<QuestionAttempt,String> {
}
