package com.lms.lms.repo;

import com.lms.lms.modals.Courses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CoursesRepo extends JpaRepository<Courses, Long> {

    Optional<Courses> findBySlug(String slug);

    Page<Courses> findByCategoryId(String categoryId, Pageable pageable);

    List<Courses> findAllByUserId(String userId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Courses c set c.isFeatured = false where c.isFeatured = true")
    void resetAllFeatured();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Courses c set c.isFeatured = true where c.id in :ids")
    void markFeaturedByIds(@Param("ids") Collection<Long> ids);

    @Query("""
            SELECT c FROM Courses c
            WHERE (:q IS NULL OR lower(c.title) LIKE lower(concat('%', :q, '%')) OR lower(c.description) LIKE lower(concat('%', :q, '%')))
              AND (:categoryId IS NULL OR c.category.id = :categoryId)
              AND (:level IS NULL OR c.level = :level)
              AND (:featured IS NULL OR c.isFeatured = :featured)
              AND (:minRating IS NULL OR (SELECT AVG(r.rating) FROM Ratings r WHERE r.course.id = c.id) >= :minRating)
              AND (:price IS NULL
                   OR (:price = 'free' AND COALESCE((SELECT MIN(p.price) FROM Pricing_Plans p WHERE p.courses.id = c.id), 0) = 0)
                   OR (:price = 'paid' AND COALESCE((SELECT MIN(p.price) FROM Pricing_Plans p WHERE p.courses.id = c.id), 0) > 0))
              AND (:maxPrice IS NULL OR COALESCE((SELECT MIN(p.price) FROM Pricing_Plans p WHERE p.courses.id = c.id), 0) <= :maxPrice)
            """)
    Page<Courses> searchCourses(@Param("q") String q,
                                @Param("categoryId") String categoryId,
                                @Param("level") Courses.Level level,
                                @Param("featured") Boolean featured,
                                @Param("minRating") Double minRating,
                                @Param("price") String price,
                                @Param("maxPrice") Double maxPrice,
                                Pageable pageable);

    Page<Courses> findByCategoryIdAndIdNot(String categoryId, Long id, Pageable pageable);

    Page<Courses> findByIsFeaturedTrue(Pageable pageable);

    List<Courses> findTop5ByTitleContainingIgnoreCase(String title);

    @Query("""
            SELECT c FROM Courses c
            WHERE (:q IS NULL OR lower(c.title) LIKE lower(concat('%', :q, '%')) OR lower(c.description) LIKE lower(concat('%', :q, '%')))
              AND (:hasCategories = false OR c.category.id IN :categoryIds)
              AND (:hasLevels = false OR c.level IN :levels)
            """)
    Page<Courses> searchCatalog(@Param("q") String q,
                                @Param("hasCategories") boolean hasCategories,
                                @Param("categoryIds") List<String> categoryIds,
                                @Param("hasLevels") boolean hasLevels,
                                @Param("levels") List<Courses.Level> levels,
                                Pageable pageable);

    @Query("""
            SELECT c.category.id, c.category.name, COUNT(c) FROM Courses c
            WHERE (:q IS NULL OR lower(c.title) LIKE lower(concat('%', :q, '%')) OR lower(c.description) LIKE lower(concat('%', :q, '%')))
            GROUP BY c.category.id, c.category.name
            """)
    List<Object[]> countByCategoryForSearch(@Param("q") String q);

    @Query("""
            SELECT c.level, COUNT(c) FROM Courses c
            WHERE c.level IS NOT NULL
              AND (:q IS NULL OR lower(c.title) LIKE lower(concat('%', :q, '%')) OR lower(c.description) LIKE lower(concat('%', :q, '%')))
            GROUP BY c.level
            """)
    List<Object[]> countByLevelForSearch(@Param("q") String q);
}

