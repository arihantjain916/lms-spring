package com.lms.lms.modals;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

public class Ratings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Integer rating;

    private String comment;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Courses courses;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
