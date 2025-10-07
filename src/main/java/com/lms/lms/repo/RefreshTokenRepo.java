package com.lms.lms.repo;

import com.lms.lms.modals.RefreshToken;
import com.lms.lms.modals.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, String> {

    RefreshToken findByTokenOrderByCreatedAtDesc(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(User user);
};
