package com.lms.lms.dto.request;


import com.lms.lms.modals.Blog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlogReq {

    private String id;

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

    @NotNull(message = "Category is required")
    private Blog.Category category;

    @NotNull(message = "Tag is required")
    private String tag;

    @NotNull(message = "Status is required")
    private Blog.Staus status;

    @NotBlank(message = "ImageUrl is required")
    private String image_url;

}
