package com.lms.lms.repo;

import com.lms.lms.modals.CourseQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseQuestionRepo extends JpaRepository<CourseQuestion, String> {

    Page<CourseQuestion> findByCourse_Id(Long courseId, Pageable pageable);

    @Query("""
            SELECT q FROM CourseQuestion q
            WHERE q.course.id = :courseId
            ORDER BY (SELECT COUNT(h) FROM QuestionHelpful h WHERE h.question.id = q.id) DESC, q.createdAt DESC
            """)
    Page<CourseQuestion> findByCourseIdOrderByHelpful(@Param("courseId") Long courseId, Pageable pageable);
}
