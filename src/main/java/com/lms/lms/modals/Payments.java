package com.lms.lms.modals;

import jakarta.persistence.*;
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
    private User user;

    @ManyToOne
    private Courses course;

    @ManyToOne
    private Pricing_Plans pricingPlan;

    private Double amount;
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
