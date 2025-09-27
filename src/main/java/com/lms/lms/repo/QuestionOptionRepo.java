package com.lms.lms.repo;

import com.lms.lms.modals.QuestionOptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepo extends JpaRepository<QuestionOptions, String> {

    List<QuestionOptions> findByQuestions_Id(String questionId);
}
