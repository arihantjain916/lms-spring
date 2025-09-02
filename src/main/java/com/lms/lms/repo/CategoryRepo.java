package com.lms.lms.repo;

import com.lms.lms.modals.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT c, COUNT(co) FROM Category c LEFT JOIN c.courses co GROUP BY c")
    List<Object[]> findCategoryCourseCounts();
}
