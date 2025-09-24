package com.lms.lms.repo;

import com.lms.lms.modals.CourseStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface CourseStatsRepo extends JpaRepository<CourseStats, Long> {

    Long countByCourseId_Id(Long courseId);

    List<CourseStats> findByCourseId_Id(Long courseId);

    Boolean existsByCourseId_IdAndUserId(Long courseId, String userId);

    List<CourseStats> findByCreatedAt(Date createdAt);
}
