package com.lms.lms.repo;

import com.lms.lms.modals.ReportCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCardRepo extends JpaRepository<ReportCard, String> {
    ReportCard findByUser_IdAndExam_Id(String userId, String examId);
}
