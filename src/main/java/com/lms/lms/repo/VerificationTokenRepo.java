package com.lms.lms.repo;

import com.lms.lms.modals.User;
import com.lms.lms.modals.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByTokenAndType(String token, VerificationToken.TokenType type);

    int deleteByUserAndType(User user, VerificationToken.TokenType type);
}
