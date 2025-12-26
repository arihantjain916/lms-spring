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
public class BlogMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "blog_id", nullable = false, unique = true)
    private Blog blog;

    // SEO
    @Column(nullable = false, length = 60)
    private String seoTitle;

    @Column(nullable = false, length = 160)
    private String seoDescription;

    // Social
    private String ogTitle;
    private String ogDescription;
    private String ogImageUrl;

    // Analytics
    private long views = 0;
    private long likes = 0;
    private long shares = 0;

    // Control flags
    private boolean indexable = true;
    private boolean followLinks = true;
    private boolean featured = false;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
