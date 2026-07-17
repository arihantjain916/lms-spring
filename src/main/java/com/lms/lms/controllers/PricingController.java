package com.lms.lms.controllers;

import com.lms.lms.dto.request.PricingPlanReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PricingPlanRes;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Pricing_Plans;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.PricingRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private PricingRepo pricingRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Default> getPlansByCourse(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            List<PricingPlanRes> plans = pricingRepo.findByCourses_IdOrderByPriceAsc(courseId)
                    .stream()
                    .map(this::toPricingPlanRes)
                    .toList();

            return ResponseEntity.ok(new Default("Pricing Plans Fetched Successfully", true, null, plans));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/course/{courseId}")
    public ResponseEntity<Default> createPlan(@PathVariable Long courseId, @Valid @RequestBody PricingPlanReq req) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManageCourse(course)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            Pricing_Plans.PlanType planType = Pricing_Plans.PlanType.valueOf(req.getPlanType());
            if (pricingRepo.existsByCourses_IdAndPlanType(courseId, planType)) {
                return ResponseEntity.badRequest().body(new Default("A " + planType.name() + " Plan Already Exists For This Course", false, null, null));
            }

            Pricing_Plans plan = new Pricing_Plans();
            plan.setCourses(course);
            plan.setTitle(req.getTitle());
            plan.setDescription(req.getDescription());
            plan.setCurrency(req.getCurrency());
            plan.setPrice(req.getPrice());
            plan.setPlanType(planType);
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Created Successfully", true, null, this.toPricingPlanRes(plan)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/{planId}")
    public ResponseEntity<Default> updatePlan(@PathVariable String planId, @Valid @RequestBody PricingPlanReq req) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManageCourse(plan.getCourses())) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            Pricing_Plans.PlanType planType = Pricing_Plans.PlanType.valueOf(req.getPlanType());
            if (planType != plan.getPlanType()
                    && pricingRepo.existsByCourses_IdAndPlanType(plan.getCourses().getId(), planType)) {
                return ResponseEntity.badRequest().body(new Default("A " + planType.name() + " Plan Already Exists For This Course", false, null, null));
            }

            plan.setTitle(req.getTitle());
            plan.setDescription(req.getDescription());
            plan.setCurrency(req.getCurrency());
            plan.setPrice(req.getPrice());
            plan.setPlanType(planType);
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Updated Successfully", true, null, this.toPricingPlanRes(plan)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/{planId}")
    public ResponseEntity<Default> deletePlan(@PathVariable String planId) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManageCourse(plan.getCourses())) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            pricingRepo.delete(plan);
            return ResponseEntity.ok(new Default("Pricing Plan Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // an instructor may only price their own courses; admins may price any
    private boolean canManageCourse(Courses course) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findById(principal.getUsername()).orElse(null);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        return course.getUser() != null && course.getUser().getId().equals(currentUser.getId());
    }

    private PricingPlanRes toPricingPlanRes(Pricing_Plans plan) {
        return new PricingPlanRes(
                plan.getId(),
                plan.getCourses().getId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getCurrency(),
                plan.getPrice(),
                plan.getPlanType().name(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
