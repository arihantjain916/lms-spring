package com.lms.lms.repo;

import com.lms.lms.modals.Courses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursesRepo extends JpaRepository<Courses , Long> {
}
