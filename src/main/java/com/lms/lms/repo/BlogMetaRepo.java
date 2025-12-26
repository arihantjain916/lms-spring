package com.lms.lms.repo;

import com.lms.lms.modals.BlogMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogMetaRepo extends JpaRepository<BlogMeta, String> {
    boolean existsByBlog_Id(String blogId);

    Optional<BlogMeta> findByBlog_Id(String blogId);
}
