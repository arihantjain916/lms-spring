package com.lms.lms.repo;

import com.lms.lms.modals.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepo extends JpaRepository<ContactUs, String> {
}
