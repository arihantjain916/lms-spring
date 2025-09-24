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
@Table(name = "course_views", uniqueConstraints = @UniqueConstraint(name = "uk_course_user", columnNames = {"course_id", "user_id"}))

public class CourseStats {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses courseId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;
}
