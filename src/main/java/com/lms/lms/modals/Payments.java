package com.lms.lms.modals;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "payments")
public class Payments {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @ManyToOne
    private Pricing_Plans pricingPlan;

    @Min(0)
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;


    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date updatedAt = new Date();


    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED
    }
}
