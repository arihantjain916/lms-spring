package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Boolean isCompleted = false;
    private Boolean isAttempt = false;

    @Enumerated(EnumType.STRING)
    private GradingStatus gradingStatus = GradingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String gradingFeedback;

    private Date gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ArrayList<QuestionAttempt> questionAttempts;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    public enum GradingStatus {
        PENDING,
        IN_PROGRESS,
        FINALIZED
    }
}
