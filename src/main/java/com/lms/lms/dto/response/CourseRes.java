package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CourseRes {
    private Long id;
    private String slug;
    private String title;
    private String description;

    private Double price;

    private UserRes user;

    private CustomCategoryRes category;
}

