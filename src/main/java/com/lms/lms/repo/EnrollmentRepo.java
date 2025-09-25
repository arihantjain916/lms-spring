package com.lms.lms.repo;

import com.lms.lms.modals.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepo extends JpaRepository<Enrollment, String> {

    Boolean existsByUser_IdAndCourses_Id(String userId, Long courseId);

    void deleteByUser_IdAndCourses_Id(String userId, Long courseId);

    Page<Enrollment> findByUser_Id(String userId, Pageable page);
//
//    List<Enrollment> findByCourses_Id(Long courseId);
}
