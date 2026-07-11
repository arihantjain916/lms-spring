package com.lms.lms.repo;

import com.lms.lms.modals.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepo extends JpaRepository<Wishlist, String> {

    Boolean existsByUser_IdAndCourses_Id(String userId, Long courseId);

    void deleteByUser_IdAndCourses_Id(String userId, Long courseId);

    Page<Wishlist> findByUser_Id(String userId, Pageable page);

    int deleteByUser_Id(String userId);
}
