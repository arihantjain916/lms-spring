package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date expiresAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }
}
