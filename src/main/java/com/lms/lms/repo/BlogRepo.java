package com.lms.lms.repo;

import com.lms.lms.modals.Blog;
import com.lms.lms.modals.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlogRepo extends JpaRepository<Blog, String> {

    @Query("SELECT b FROM Blog b WHERE b.user.id = :userId")
    List<Blog> findAllByUserId(@Param("userId") String userId);

    @Query("""
                SELECT b FROM Blog b 
                WHERE (:user IS NULL OR b.user = :user) 
                  AND (:status IS NULL OR b.status = :status)
            """)
    List<Blog> findByUserandStatus(User user, Blog.Staus status);
}
