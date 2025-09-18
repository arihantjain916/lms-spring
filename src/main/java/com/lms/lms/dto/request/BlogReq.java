package com.lms.lms.dto.request;


import com.lms.lms.modals.Blog;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlogReq {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Read Time is required")
    private String read_time;

    @NotBlank(message = "Tag is required")
    private Blog.Tag tag;

    @NotBlank(message = "Status is required")
    private Blog.Staus status;

    @NotBlank(message = "ImageUrl is required")
    private String imageUrl;
}
