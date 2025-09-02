package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CourseRes {
    private Long id;
    private String slug;
    private String title;
    private String description;

    private UserRes user;

    private CustomCategoryRes category;
}

