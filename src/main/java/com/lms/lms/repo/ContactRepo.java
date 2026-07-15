package com.lms.lms.repo;

import com.lms.lms.modals.ContactUs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepo extends JpaRepository<ContactUs, String> {

    @Query("""
            SELECT c FROM ContactUs c
            WHERE (:department IS NULL OR c.department = :department)
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<ContactUs> adminSearch(@Param("department") ContactUs.Department department,
                                @Param("status") ContactUs.Status status,
                                Pageable pageable);
}
