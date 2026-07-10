package com.lms.lms.repo;

import com.lms.lms.modals.QuestionHelpful;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionHelpfulRepo extends JpaRepository<QuestionHelpful, String> {

    Boolean existsByQuestion_IdAndUser_Id(String questionId, String userId);

    int deleteByQuestion_IdAndUser_Id(String questionId, String userId);

    Integer countByQuestion_Id(String questionId);

    int deleteByQuestion_Id(String questionId);
}
