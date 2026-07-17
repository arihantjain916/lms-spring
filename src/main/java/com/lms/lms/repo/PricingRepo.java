package com.lms.lms.repo;

import com.lms.lms.modals.Pricing_Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PricingRepo extends JpaRepository<Pricing_Plans, String> {

    @Query("SELECT MIN(p.price) FROM Pricing_Plans p WHERE p.courses.id = :courseId")
    Double getMinPlanPriceByCourseId(Long courseId);

    Optional<Pricing_Plans> findFirstByCourses_IdOrderByPriceAsc(Long courseId);

    List<Pricing_Plans> findByCourses_IdOrderByPriceAsc(Long courseId);

    boolean existsByCourses_IdAndPlanType(Long courseId, Pricing_Plans.PlanType planType);
}
