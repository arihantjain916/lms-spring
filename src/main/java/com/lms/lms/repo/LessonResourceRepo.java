package com.lms.lms.repo;

import com.lms.lms.modals.LessonResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonResourceRepo extends JpaRepository<LessonResource, String> {

    List<LessonResource> findByLesson_IdOrderByCreatedAtAsc(String lessonId);
}
