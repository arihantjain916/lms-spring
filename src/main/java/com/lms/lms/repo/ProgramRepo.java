package com.lms.lms.repo;

import com.lms.lms.modals.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepo extends JpaRepository<Program, String> {

    Page<Program> findByIsActiveTrue(Pageable pageable);

    Optional<Program> findBySlug(String slug);
}
