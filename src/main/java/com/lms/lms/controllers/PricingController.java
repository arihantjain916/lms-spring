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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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

    // the reusable catalog: every plan, whatever it is attached to
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("")
    @Transactional
    public ResponseEntity<Default> getAllPlans() {
        try {
            List<PricingPlanRes> plans = pricingRepo.findAll(Sort.by("price").ascending())
                    .stream()
                    .map(this::toPricingPlanRes)
                    .toList();
            return ResponseEntity.ok(new Default("Pricing Plans Fetched Successfully", true, null, plans));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // the response lists every course on each plan, so keep the session open for it
    @GetMapping("/course/{courseId}")
    @Transactional
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

    // creates a plan and attaches it to one course; use the attach endpoint to reuse it elsewhere
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/course/{courseId}")
    @Transactional
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
            plan.setTitle(req.getTitle());
            plan.setDescription(req.getDescription());
            plan.setCurrency(req.getCurrency());
            plan.setPrice(req.getPrice());
            plan.setPlanType(planType);
            plan.getCourses().add(course);
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Created Successfully", true, null, this.toPricingPlanRes(plan)));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // reuses an existing plan on another course, so a price point is defined once
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/{planId}/courses/{courseId}")
    @Transactional
    public ResponseEntity<Default> attachPlanToCourse(@PathVariable String planId, @PathVariable Long courseId) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            // attaching only changes this course, so owning the course is enough
            if (!this.canManageCourse(course)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            if (pricingRepo.existsByIdAndCourses_Id(planId, courseId)) {
                return ResponseEntity.badRequest().body(new Default("Plan Already Attached To This Course", false, null, null));
            }

            if (pricingRepo.existsByCourses_IdAndPlanType(courseId, plan.getPlanType())) {
                return ResponseEntity.badRequest().body(new Default("A " + plan.getPlanType().name() + " Plan Already Exists For This Course", false, null, null));
            }

            plan.getCourses().add(course);
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Attached Successfully", true, null, this.toPricingPlanRes(plan)));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/{planId}/courses/{courseId}")
    @Transactional
    public ResponseEntity<Default> detachPlanFromCourse(@PathVariable String planId, @PathVariable Long courseId) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManageCourse(course)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            if (!pricingRepo.existsByIdAndCourses_Id(planId, courseId)) {
                return ResponseEntity.badRequest().body(new Default("Plan Is Not Attached To This Course", false, null, null));
            }

            plan.getCourses().removeIf(c -> c.getId().equals(courseId));
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Detached Successfully", true, null, null));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/{planId}")
    @Transactional
    public ResponseEntity<Default> updatePlan(@PathVariable String planId, @Valid @RequestBody PricingPlanReq req) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManagePlan(plan)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to edit this pricing plan", false, null, null));
            }

            Pricing_Plans.PlanType planType = Pricing_Plans.PlanType.valueOf(req.getPlanType());
            if (planType != plan.getPlanType()) {
                // the new type must not collide on any course already using this plan
                for (Courses course : plan.getCourses()) {
                    if (pricingRepo.existsByCourses_IdAndPlanType(course.getId(), planType)) {
                        return ResponseEntity.badRequest().body(new Default("A " + planType.name() + " Plan Already Exists For Course: " + course.getTitle(), false, null, null));
                    }
                }
            }

            plan.setTitle(req.getTitle());
            plan.setDescription(req.getDescription());
            plan.setCurrency(req.getCurrency());
            plan.setPrice(req.getPrice());
            plan.setPlanType(planType);
            pricingRepo.save(plan);

            return ResponseEntity.ok(new Default("Pricing Plan Updated Successfully", true, null, this.toPricingPlanRes(plan)));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/{planId}")
    @Transactional
    public ResponseEntity<Default> deletePlan(@PathVariable String planId) {
        try {
            Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
            if (plan == null) {
                return new ResponseEntity<>(new Default("Pricing Plan Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManagePlan(plan)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to delete this pricing plan", false, null, null));
            }

            // deleting a shared plan would silently reprice every other course on it
            long attached = pricingRepo.countAttachedCourses(planId);
            if (attached > 1) {
                return ResponseEntity.badRequest().body(new Default("Plan Is Shared By " + attached + " Courses. Detach It Instead", false, null, null));
            }

            pricingRepo.delete(plan);
            return ResponseEntity.ok(new Default("Pricing Plan Deleted Successfully", true, null, null));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // an instructor may only manage their own courses; admins may manage any
    private boolean canManageCourse(Courses course) {
        User currentUser = this.currentUser();
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        return course.getUser() != null && course.getUser().getId().equals(currentUser.getId());
    }

    // editing a plan reprices every course attached to it, so an instructor may only edit
    // a plan that is theirs alone. Admins may edit any; unattached plans are admin-only.
    private boolean canManagePlan(Pricing_Plans plan) {
        User currentUser = this.currentUser();
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (plan.getCourses().isEmpty()) {
            return false;
        }
        return plan.getCourses().stream()
                .allMatch(course -> course.getUser() != null && course.getUser().getId().equals(currentUser.getId()));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return userRepo.findById(principal.getUsername()).orElse(null);
    }

    private PricingPlanRes toPricingPlanRes(Pricing_Plans plan) {
        return new PricingPlanRes(
                plan.getId(),
                plan.getCourses().stream().map(Courses::getId).toList(),
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
