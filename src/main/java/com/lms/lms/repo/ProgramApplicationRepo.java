package com.lms.lms.repo;

import com.lms.lms.modals.ProgramApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramApplicationRepo extends JpaRepository<ProgramApplication, String> {

    Boolean existsByProgram_IdAndEmail(String programId, String email);

    Page<ProgramApplication> findByProgram_Id(String programId, Pageable pageable);
}
