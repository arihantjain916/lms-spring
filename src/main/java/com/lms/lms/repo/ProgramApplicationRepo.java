package com.lms.lms.repo;

import com.lms.lms.modals.ProgramApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramApplicationRepo extends JpaRepository<ProgramApplication, String> {

    Boolean existsByProgram_IdAndEmail(String programId, String email);
}
