package com.lms.lms.controllers;


import com.lms.lms.dto.request.BlogReq;
import com.lms.lms.dto.response.BlogRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.mappers.BlogMapper;
import com.lms.lms.modals.Blog;
import com.lms.lms.modals.User;
import com.lms.lms.repo.BlogRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogRepo blogRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BlogMapper blogMapper;

    public User userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        return userRepo.findById(user.getUsername()).orElse(null);
    }


    @GetMapping
    public ResponseEntity<?> getAllBlogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category
    ) {
        try {
            Blog.Category categoryValue = null;
            User user = null;

            if (category != null && !category.isEmpty()) {
                try {
                    categoryValue = Blog.Category.valueOf(category);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Status", false, null, null));
                }
            }
            if (userId != null && !userId.isEmpty()) {
                var isUserExist = userRepo.findById(userId).orElse(null);
                if (isUserExist == null) {
                    return ResponseEntity.badRequest().body(new Default("User don't exist", false, null, null));
                }
                user = isUserExist;
            }

            List<Blog> blogs = blogRepo.findByUserandTag(user, categoryValue);

            List<BlogRes> blogRes = blogs.
                    stream()
                    .map(blogMapper::toDto)
                    .toList();
            return ResponseEntity.ok().body(new Default("Blog Fetched Successfully", true, null, blogRes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Default> save(@Valid @RequestBody BlogReq blogReq) {
        try {
            var isUserExist = userDetails();

            if (isUserExist == null) {
                return ResponseEntity.badRequest().body(new Default("User don't exist", false, null, null));
            }

            var isSlugExist = blogRepo.findBySlug(blogReq.getSlug()).orElse(null);
            if (isSlugExist != null) {
                return ResponseEntity.badRequest().body(new Default("Blog already exist with same slug", false, null, null));
            }

            var isTitleExist = blogRepo.findByTitle(blogReq.getTitle()).orElse(null);
            if (isTitleExist != null) {
                return ResponseEntity.badRequest().body(new Default("Blog already exist with same Title", false, null, null));
            }

            Blog blog = new Blog();
            blog.setTitle(blogReq.getTitle());
            blog.setSlug(blogReq.getSlug());
            blog.setDescription(blogReq.getDescription());
            blog.setContent(blogReq.getContent());
            blog.setRead_time(blogReq.getRead_time());
            blog.setStatus(blogReq.getStatus());
            blog.setTag(blogReq.getTag());
            blog.setUser(isUserExist);
            blog.setCategory(blogReq.getCategory());
            blog.setImageUrl(blogReq.getImage_url());
            blogRepo.save(blog);

            return ResponseEntity.ok().body(new Default("Blog Added Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Default> updateBlogById(@Valid @RequestBody BlogReq blogReq) {
        try {
            if (blogReq.getId() == null || blogReq.getId().isEmpty()) {
                return ResponseEntity.badRequest().body(new Default("Blog Id is required", false, null, null));
            }

            var isBlogExist = blogRepo.findById(blogReq.getId()).orElse(null);

            if (isBlogExist == null) {
                return ResponseEntity.badRequest().body(new Default("Blog Not Found", false, null, null));
            }

            var user = userDetails();

            boolean isBlogOwner = user.getId().equals(isBlogExist.getUser().getId());
            if (!isBlogOwner) {
                return ResponseEntity.badRequest().body(new Default("You are not authorized to update this blog", false, null, null));
            }

            var isSlugExist = blogRepo.findBySlug(blogReq.getSlug()).orElse(null);
            if (isSlugExist != null && !isSlugExist.getId().equals(blogReq.getId())) {
                return ResponseEntity.badRequest().body(new Default("Blog already exist with same slug", false, null, null));
            }

            var isTitleExist = blogRepo.findByTitle(blogReq.getTitle()).orElse(null);
            if (isTitleExist != null && !isTitleExist.getId().equals(blogReq.getId())) {
                return ResponseEntity.badRequest().body(new Default("Blog already exist with same Title", false, null, null));
            }

            isBlogExist.setSlug(blogReq.getSlug());
            isBlogExist.setTitle(blogReq.getTitle());
            isBlogExist.setDescription(blogReq.getDescription());
            isBlogExist.setContent(blogReq.getContent());
            isBlogExist.setRead_time(blogReq.getRead_time());
            isBlogExist.setStatus(blogReq.getStatus());
            isBlogExist.setTag(blogReq.getTag());
            isBlogExist.setImageUrl(blogReq.getImage_url());
            isBlogExist.setUser(isBlogExist.getUser());
            blogRepo.save(isBlogExist);
            return ResponseEntity.ok().body(new Default("Blog Updated Successfully", true, null, null));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteBlogById(@PathVariable String id) {
        try {
            var user = userDetails();
            var isBlogExist = blogRepo.findById(id).orElse(null);
            if (isBlogExist == null) {
                return ResponseEntity.badRequest().body(new Default("Blog Not Found", false, null, null));
            }

            boolean isBlogOwner = user.getId().equals(isBlogExist.getUser().getId());
            if (!isBlogOwner) {
                return ResponseEntity.badRequest().body(new Default("You are not authorized to delete this blog", false, null, null));
            }
            blogRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Blog Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Default> getUserBlog() {
        try {
            var user = userDetails();
            List<BlogRes> blogRes = blogRepo.findAllByUserId(user.getId()).stream().map(blogMapper::toDto).toList();

            return ResponseEntity.ok().body(new Default("Blog Fetched Successfully", true, null, blogRes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
