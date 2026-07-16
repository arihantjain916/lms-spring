package com.lms.lms.repo;

import com.lms.lms.modals.ReportCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportCardRepo extends JpaRepository<ReportCard, String> {
    ReportCard findByUser_IdAndExam_Id(String userId, String examId);

    List<ReportCard> findByUser_Id(String userId);

    List<ReportCard> findByExam_Id(String userId);

    Optional<ReportCard> findByIdAndUser_Id(String id, String userId);
}
