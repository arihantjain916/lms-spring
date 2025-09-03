package com.lms.lms.repo;

import com.lms.lms.modals.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepo extends JpaRepository<Payments, String> {
}
