package com.lms.lms.repo;

import com.lms.lms.modals.Pricing_Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PricingRepo extends JpaRepository<Pricing_Plans, String> {

    @Query("SELECT MIN(p.price) FROM Pricing_Plans p WHERE p.courses.id = :courseId")
    Double getMinPlanPriceByCourseId(Long courseId);
}
