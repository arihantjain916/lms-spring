package com.lms.lms.repo;

import com.lms.lms.modals.Exam;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamRepo extends JpaRepository<Exam, String> {

    List<Exam> findByCourses_IdAndStatus(Long courseId, Exam.Staus status);

    List<Exam> findByUser_Id(String courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Exam e set e.status = :status where e.id = :examId")
    void updateStaus(String examId, Exam.Staus status);
}
