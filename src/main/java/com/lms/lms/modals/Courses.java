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
public class Courses {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String description;

    @Column(nullable = true)
    private Boolean isFeatured = false;

    @ManyToOne
    @JoinColumn(name = "category_id",  nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @ManyToOne
    private Pricing_Plans pricingPlan;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();
    //to do add asset ids
}
