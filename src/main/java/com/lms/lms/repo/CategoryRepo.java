package com.lms.lms.repo;

import com.lms.lms.modals.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
}
