package com.lms.lms.repo;

import com.lms.lms.modals.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, String> {
}
