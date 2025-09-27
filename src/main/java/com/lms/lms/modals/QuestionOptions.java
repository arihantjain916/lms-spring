package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class QuestionOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String option;

    @Column(nullable = false)
    private Boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Questions questions;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();
}
