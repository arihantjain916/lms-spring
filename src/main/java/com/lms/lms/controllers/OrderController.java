package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.OrderRes;
import com.lms.lms.modals.Payments;
import com.lms.lms.modals.User;
import com.lms.lms.repo.PaymentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/{orderId}")
    public ResponseEntity<Default> getOrderById(@PathVariable String orderId) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Payments payment = paymentRepo.findById(orderId).orElse(null);
            if (payment == null) {
                return new ResponseEntity<>(new Default("Order Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            boolean isOwner = payment.getUser().getId().equals(user.getId());
            if (!isOwner && user.getRole() != User.Role.ADMIN) {
                return new ResponseEntity<>(new Default("Order Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            OrderRes res = new OrderRes(
                    payment.getId(),
                    payment.getCourse().getId(),
                    payment.getCourse().getTitle(),
                    payment.getPricingPlan() != null ? payment.getPricingPlan().getId() : null,
                    payment.getPricingPlan() != null ? payment.getPricingPlan().getTitle() : null,
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus().name(),
                    payment.getPaymentReference(),
                    payment.getCreatedAt()
            );
            return ResponseEntity.ok(new Default("Order Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
