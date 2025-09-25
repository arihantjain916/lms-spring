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

public interface CoursesRepo extends JpaRepository<Courses , Long> {

    Optional<Courses> findBySlug(String slug);

    Page<Courses> findByCategoryId(String categoryId, Pageable pageable);

    List<Courses> findAllByUserId(String userId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Courses c set c.isFeatured = false where c.isFeatured = true")
    void resetAllFeatured();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Courses c set c.isFeatured = true where c.id in :ids")
    void markFeaturedByIds(@Param("ids") Collection<Long> ids);
}

