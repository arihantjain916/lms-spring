package com.lms.lms.repo;

import com.lms.lms.modals.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepo extends JpaRepository<Exam, String> {

    List<Exam> findByCourses_IdAndStatus(Long courseId, Exam.Staus status);

    List<Exam> findByUser_Id(String courseId);
}
