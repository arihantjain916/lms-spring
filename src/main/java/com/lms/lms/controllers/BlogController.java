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
            @RequestParam(required = false) String status
    ) {
        try {
            Blog.Staus statusValue = null;
            User user = null;

            if (status != null && !status.isEmpty()) {
                try {
                    statusValue = Blog.Staus.valueOf(status);
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


            List<Blog> blogs = blogRepo.findByUserandStatus(user, statusValue);

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

            Blog blog = new Blog();
            blog.setTitle(blogReq.getTitle());
            blog.setSlug(blogReq.getSlug());
            blog.setDescription(blogReq.getDescription());
            blog.setUser(isUserExist);
            blog.setContent(blogReq.getContent());
            blogRepo.save(blog);

            return ResponseEntity.ok().body(new Default("Blog Added Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
