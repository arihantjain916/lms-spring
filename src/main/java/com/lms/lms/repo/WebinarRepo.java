package com.lms.lms.repo;

import com.lms.lms.modals.Webinar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WebinarRepo extends JpaRepository<Webinar, String> {

    Optional<Webinar> findBySlug(String slug);

    List<Webinar> findTop5ByTitleContainingIgnoreCase(String title);

    @Query("""
            SELECT w FROM Webinar w
            WHERE (:q IS NULL OR lower(w.title) LIKE lower(concat('%', :q, '%')) OR lower(w.description) LIKE lower(concat('%', :q, '%')))
              AND (:categoryId IS NULL OR w.category.id = :categoryId)
              AND (:status IS NULL
                   OR (:status = 'upcoming' AND w.scheduledAt >= :now)
                   OR (:status = 'past' AND w.scheduledAt < :now))
            """)
    Page<Webinar> searchWebinars(@Param("q") String q,
                                 @Param("categoryId") String categoryId,
                                 @Param("status") String status,
                                 @Param("now") Date now,
                                 Pageable pageable);

    @Query("""
            SELECT w FROM Webinar w
            WHERE (:q IS NULL OR lower(w.title) LIKE lower(concat('%', :q, '%')) OR lower(w.description) LIKE lower(concat('%', :q, '%')))
              AND (:hasCategories = false OR w.category.id IN :categoryIds)
            """)
    Page<Webinar> searchCatalog(@Param("q") String q,
                                @Param("hasCategories") boolean hasCategories,
                                @Param("categoryIds") List<String> categoryIds,
                                Pageable pageable);
}
