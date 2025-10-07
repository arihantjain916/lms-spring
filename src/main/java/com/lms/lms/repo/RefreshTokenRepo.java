package com.lms.lms.repo;

import com.lms.lms.modals.RefreshToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, String> {

    RefreshToken findByTokenOrderByCreatedAtDesc(String token, Pageable pageable);
};
