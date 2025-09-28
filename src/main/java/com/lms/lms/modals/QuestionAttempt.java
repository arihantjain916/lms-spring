package com.lms.lms.modals;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String answer;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Questions questions;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date attemptedAt = new Date();
}
