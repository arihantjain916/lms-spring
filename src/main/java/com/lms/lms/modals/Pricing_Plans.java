package com.lms.lms.modals;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Double price;

    @Column(nullable = false)
    private PlanType planType = PlanType.LIFETIME;

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
