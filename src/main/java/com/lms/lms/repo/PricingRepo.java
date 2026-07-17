package com.lms.lms.repo;

import com.lms.lms.modals.Pricing_Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PricingRepo extends JpaRepository<Pricing_Plans, String> {

    @Query("SELECT MIN(p.price) FROM Pricing_Plans p JOIN p.courses c WHERE c.id = :courseId")
    Double getMinPlanPriceByCourseId(Long courseId);

    Optional<Pricing_Plans> findFirstByCourses_IdOrderByPriceAsc(Long courseId);

    List<Pricing_Plans> findByCourses_IdOrderByPriceAsc(Long courseId);

    boolean existsByCourses_IdAndPlanType(Long courseId, Pricing_Plans.PlanType planType);

    boolean existsByIdAndCourses_Id(String planId, Long courseId);

    // a shared plan must not be deleted out from under the other courses using it
    @Query("SELECT COUNT(c) FROM Pricing_Plans p JOIN p.courses c WHERE p.id = :planId")
    long countAttachedCourses(String planId);
}
