package com.lms.lms.repo;

import com.lms.lms.modals.CourseStats;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CourseStatsRepo extends JpaRepository<CourseStats, Long> {

    Long countByCourseId_Id(Long courseId);

    List<CourseStats> findByCourseId_Id(Long courseId);

    Boolean existsByCourseId_IdAndUserId(Long courseId, String userId);

    List<CourseStats> findByCreatedAt(Date createdAt);

    @Query("""
            select cs.courseId.id
            from CourseStats cs
            where cs.createdAt >= :from and cs.createdAt < :to
            group by cs.courseId.id
            order by sum(cs.id) desc, cs.courseId.id asc
            """)
    List<Long> topCourseIdsByViewsInWindow(@Param("from") Date from,
                                           @Param("to") Date to,
                                           PageRequest pageable);


//    @Query(value = """
//       SELECT cs.course_id
//       FROM course_stats cs
//       WHERE cs.created_at >= :from AND cs.created_at < :to
//       GROUP BY cs.course_id
//       ORDER BY COUNT(cs.id) DESC, cs.course_id ASC
//       LIMIT :limit
//       """, nativeQuery = true)
//    List<Long> findTopCourseIds(@Param("from") Date from,
//                                @Param("to") Date to,
//                                @Param("limit") int limit);

}
