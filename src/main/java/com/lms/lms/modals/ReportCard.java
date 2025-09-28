package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class ReportCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false)
    private BigDecimal obtainedMarks;

    @Column(nullable = false)
    private BigDecimal totalMarks;

    @Column(nullable = false)
    private BigDecimal percentage;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();
}
