package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_options_id")
    private QuestionOptions options;

    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private BigDecimal marks;

    private Integer position;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    public enum Type {
        TEXT,
        MCQ,
        TRUE_FALSE,
        SHORT_ANSWER,
        LONG_ANSWER,
    }
}
