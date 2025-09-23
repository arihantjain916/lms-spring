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
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String public_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
