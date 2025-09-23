package com.lms.lms.modals;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;


    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role = Role.STUDENT;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    @OneToMany(mappedBy = "user")
    private List<Asset> assets;

    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }
}
