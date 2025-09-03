package com.lms.lms.repo;

import com.lms.lms.modals.Pricing_Plans;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRepo extends JpaRepository<Pricing_Plans, String> {
}
