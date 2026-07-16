package com.lms.lms.repo;

import com.lms.lms.modals.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:q IS NULL OR lower(u.name) LIKE lower(concat('%', :q, '%'))
                   OR lower(u.username) LIKE lower(concat('%', :q, '%'))
                   OR lower(u.email) LIKE lower(concat('%', :q, '%')))
              AND (:role IS NULL OR u.role = :role)
            """)
    Page<User> adminSearch(@Param("q") String q, @Param("role") User.Role role, Pageable pageable);

    long countByRole(User.Role role);

    // used to fan notifications out to the shared customer-care queue;
    // banned/deleted/inactive accounts must not receive them
    List<User> findByRoleAndIsActiveTrueAndIsDeletedFalseAndIsBannedFalse(User.Role role);

    // every reachable account, for admin announcements aimed at the whole user base
    List<User> findByIsActiveTrueAndIsDeletedFalseAndIsBannedFalse();
}
