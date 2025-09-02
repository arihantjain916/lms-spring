package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CourseReq {

    @NotBlank(message = "Course title is required")
    private String title;

    @NotBlank(message = "Course Slug is required")
    private String slug;

    @NotBlank(message = "Course Description is required")
    private String description;

    @NotBlank(message = "Course Category is required")
    private String categoryId;

}
