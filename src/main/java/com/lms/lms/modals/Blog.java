package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String title, slug;

    @Column(nullable = false)
    private String read_time, description, content;

    @Column(nullable = false)
    private String Tag;

    @Column(nullable = false)
    private Category category = Category.TECHNOLOGY;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Staus status = Staus.DRAFT;

    private Boolean isFeatured = Boolean.FALSE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public enum Staus {
        DRAFT,
        PUBLISHED,
        UNPUBLISHED
    }

    public enum Category {
        CAREER,
        LEARNING,
        TECHNOLOGY,
        INDUSTRY,
        EDUCATION
    }

    //Add Category
    //Add isFeatured

}
