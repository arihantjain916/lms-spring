package com.lms.lms.repo;

import com.lms.lms.modals.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepo extends JpaRepository<Certificate, String> {

    Optional<Certificate> findByUser_IdAndCourse_Id(String userId, Long courseId);

    Page<Certificate> findByUser_Id(String userId, Pageable pageable);
}
