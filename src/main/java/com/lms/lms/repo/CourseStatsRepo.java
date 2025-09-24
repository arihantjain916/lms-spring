package com.lms.lms.repo;

import com.lms.lms.modals.CourseStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseStatsRepo extends JpaRepository<CourseStats, Long> {

    Integer countByCourseId(Long courseId);

    List<CourseStats> findByCourseId(Long courseId);

    Boolean existsByCourseIdAndUserId(Long courseId, String userId);
}
