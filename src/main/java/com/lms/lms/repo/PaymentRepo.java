package com.lms.lms.repo;

import com.lms.lms.modals.Payments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepo extends JpaRepository<Payments, String> {

    Page<Payments> findByStatus(Payments.PaymentStatus status, Pageable pageable);

    long countByStatus(Payments.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payments p WHERE p.status = com.lms.lms.modals.Payments.PaymentStatus.PAID")
    Double totalRevenue();
}
