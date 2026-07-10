package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.CheckoutReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.OrderRes;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.modals.Payments;
import com.lms.lms.modals.Pricing_Plans;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.PaymentRepo;
import com.lms.lms.repo.PricingRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private PricingRepo pricingRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private UserDetails userDetails;

    @PostMapping("/session")
    @Transactional
    public ResponseEntity<Default> createCheckoutSession(@Valid @RequestBody CheckoutReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Courses course = coursesRepo.findById(req.getCourseId()).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            Boolean isUserAlreadyEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), course.getId());
            if (isUserAlreadyEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User Already Enrolled", false, null, null));
            }

            Pricing_Plans plan;
            if (req.getPricingPlanId() != null && !req.getPricingPlanId().isBlank()) {
                plan = pricingRepo.findById(req.getPricingPlanId()).orElse(null);
                if (plan == null || !plan.getCourses().getId().equals(course.getId())) {
                    return ResponseEntity.badRequest().body(new Default("Pricing Plan Not Found For This Course", false, null, null));
                }
            } else {
                plan = pricingRepo.findFirstByCourses_IdOrderByPriceAsc(course.getId()).orElse(null);
            }

            Payments payment = new Payments();
            payment.setUser(user);
            payment.setCourse(course);
            payment.setPricingPlan(plan);
            payment.setAmount(plan != null ? plan.getPrice() : 0.0);
            payment.setCurrency(plan != null ? plan.getCurrency() : "INR");

            // free courses skip the payment gateway and enroll immediately
            if (plan == null || plan.getPrice() == 0) {
                payment.setStatus(Payments.PaymentStatus.PAID);
                paymentRepo.save(payment);

                Enrollment enrollment = new Enrollment();
                enrollment.setCourses(course);
                enrollment.setUser(user);
                enrollmentRepo.save(enrollment);

                return ResponseEntity.ok().body(new Default("Enrolled Successfully", true, null, this.toOrderRes(payment)));
            }

            payment.setStatus(Payments.PaymentStatus.PENDING);
            paymentRepo.save(payment);

            return ResponseEntity.ok().body(new Default("Checkout Session Created Successfully", true, null, this.toOrderRes(payment)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private OrderRes toOrderRes(Payments payment) {
        return new OrderRes(
                payment.getId(),
                payment.getCourse().getId(),
                payment.getCourse().getTitle(),
                payment.getPricingPlan() != null ? payment.getPricingPlan().getId() : null,
                payment.getPricingPlan() != null ? payment.getPricingPlan().getTitle() : null,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}
