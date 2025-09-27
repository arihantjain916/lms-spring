package com.lms.lms.repo;

import com.lms.lms.modals.Exam;
import com.lms.lms.modals.ExamAttempt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamAttemptRepo extends JpaRepository<ExamAttempt,String> {
    List<ExamAttempt> findByUser_IdAndExam_Id(String user_id, String exam_id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ExamAttempt e set e.isCompleted = :status where e.id = :examId")
    void markExamCompete(String examId, Boolean status);
}
