package com.lms.lms.modals;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "pricing_plans")
public class Pricing_Plans {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title,description, currency;

    @Column(nullable = false)
    @Min(0)
    private Double price;

    @Column(nullable = false)
    private PlanType planType;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Courses courses;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    @Getter
    public enum PlanType {
        MONTHLY(1),
        QUARTERLY(2),
        YEARLY(2),
        LIFETIME(0);

        private final int value;

        PlanType(int value) {
            this.value = value;
        }

        public static PlanType fromValue(int value) {
            for (PlanType type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid PlanType value: " + value);
        } }
}
