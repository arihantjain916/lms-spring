package com.lms.lms.repo;

import com.lms.lms.modals.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAttemptRepo extends JpaRepository<QuestionAttempt,String> {

    List<QuestionAttempt> findByUser_IdAndExam_Id(String userId, String examId);
}
