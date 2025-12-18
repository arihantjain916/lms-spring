package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "usageDate"})
)
public class AiDailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate usageDate;

    // Limits
    private Integer dailyQuestionLimit;
    private Integer dailyTokenLimit;
    private BigDecimal dailyCostLimit;

    // Usage
    private Integer questionsUsed;
    private Integer tokensUsed;
    private BigDecimal costUsed;

    private Instant lastUpdated;
}
