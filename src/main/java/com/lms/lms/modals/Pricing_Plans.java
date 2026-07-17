package com.lms.lms.modals;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "pricing_plans")
public class Pricing_Plans {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Min(0)
    private Double price;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PlanType planType;

    // a plan is a reusable price point: one plan can be attached to many courses,
    // and a course can offer several plans (one per PlanType)
    @ManyToMany
    @JoinTable(
            name = "course_pricing_plans",
            joinColumns = @JoinColumn(name = "pricing_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Courses> courses = new LinkedHashSet<>();

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
        YEARLY(3),
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
