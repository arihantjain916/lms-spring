package com.lms.lms.repo;

import com.lms.lms.modals.Questions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepo extends JpaRepository<Questions, String> {

    List<Questions> findByExam_Id(String examId);
}
