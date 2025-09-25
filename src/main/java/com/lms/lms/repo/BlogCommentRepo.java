package com.lms.lms.repo;

import com.lms.lms.modals.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogCommentRepo extends JpaRepository<BlogComment, String> {

    List<BlogComment> findByParent_Id(String parentId);

    Page<BlogComment> findByBlogId_IdAndParentIsNull(String blogId, Pageable pageable);

//    findByBlogId_IdAndParentIsNull

    Page<BlogComment> findByBlogId_IdOrderByCreatedAtAsc(String blogId, Pageable pageable);

    Boolean existsByBlogId_IdAndUserId_Id(String blogId, String userId);
}
