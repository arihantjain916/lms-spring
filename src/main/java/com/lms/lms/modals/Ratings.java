package com.lms.lms.modals;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

//@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class Ratings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false,precision = 2, scale = 1)
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal rating;

    private String comment;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Courses course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
