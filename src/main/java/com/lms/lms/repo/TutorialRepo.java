package com.lms.lms.repo;

import com.lms.lms.modals.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TutorialRepo extends JpaRepository<Tutorial, String> {

    Optional<Tutorial> findBySlug(String slug);

    Page<Tutorial> findByUser_Id(String userId, Pageable pageable);

    List<Tutorial> findTop5ByTitleContainingIgnoreCase(String title);

    @Query("""
            SELECT t FROM Tutorial t
            WHERE (:q IS NULL OR lower(t.title) LIKE lower(concat('%', :q, '%')) OR lower(t.description) LIKE lower(concat('%', :q, '%')))
              AND (:hasCategories = false OR t.category.id IN :categoryIds)
            """)
    Page<Tutorial> searchCatalog(@Param("q") String q,
                                 @Param("hasCategories") boolean hasCategories,
                                 @Param("categoryIds") List<String> categoryIds,
                                 Pageable pageable);
}
