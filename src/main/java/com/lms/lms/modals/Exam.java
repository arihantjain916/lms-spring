package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private boolean shuffleQuestions = true;
    private boolean showScoreImmediately = false;
    private Integer maxAttempts = 1;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Column(nullable = false)
    private Integer timeLimitMin, totalMarks, passMarks;



    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private Staus status = Staus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Courses courses;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    public enum Staus {
        DRAFT,
        PUBLISHED,
        UNPUBLISHED,
        CLOSED, ARCHIVED
    }

}
