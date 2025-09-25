package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.BlogCommentReq;
import com.lms.lms.dto.response.BlogCommentRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.BlogCommentMapper;
import com.lms.lms.modals.BlogComment;
import com.lms.lms.repo.BlogCommentRepo;
import com.lms.lms.repo.BlogRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/blog")
public class BlogCommentController {

    @Autowired
    private BlogCommentRepo blogCommentRepo;

    @Autowired
    private BlogRepo blogRepo;

    @Autowired
    private BlogCommentMapper blogCommentMapper;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/{id}/comments")

    public ResponseEntity<?> findAllCommentByBlogId(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,

            @PathVariable String id
    ) {
        try {

            var isBlogExist = blogRepo.existsById(id);
            if (!isBlogExist) {
                return ResponseEntity.badRequest().body(new Default("Blog don't exist", false, null, null));
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, size, sort);

            Page<BlogComment> blogComments = blogCommentRepo.findByBlogId_IdAndParentIsNull(id, pageable);
            List<BlogCommentRes> blogCommentRes = blogComments.stream().map(blogCommentMapper::toDto).toList();

            PaginatedResponse<BlogCommentRes> paginatedResponse = new PaginatedResponse<>(
                    "Comments Fetch Successfully",
                    true,
                    blogCommentRes,
                    blogComments.getNumber() + 1,
                    blogComments.getSize(),
                    blogComments.getTotalElements(),
                    blogComments.getTotalPages()
            );

            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), null, null, null));
        }
    }

    @PostMapping("/comment")
    public ResponseEntity<Default> saveComment(@Valid @RequestBody BlogCommentReq blogComment) {
        try {
            var user = userDetails.userDetails();
            if (user == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            var blog = blogRepo.findById(blogComment.getBlogId()).orElse(null);
            if (blog == null) {
                return ResponseEntity.badRequest().body(new Default("Blog Not Found", false, null, null));
            }

            Boolean isUserAlreadyCommented = blogCommentRepo.existsByBlogId_IdAndUserId_Id(blogComment.getBlogId(), user.getId());

            if (isUserAlreadyCommented) {
                return ResponseEntity.badRequest().body(new Default("User Already Commented", false, null, null));
            }
            BlogComment blogComment1 = new BlogComment();
            blogComment1.setBlog(blog);
            blogComment1.setUser(user);
            blogComment1.setComment(blogComment.getComment());
            blogCommentRepo.save(blogComment1);

            return ResponseEntity.ok().body(new Default("Comment Added Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

}
