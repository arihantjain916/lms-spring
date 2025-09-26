package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomCourseRes {
    private Long id;
    private String title;
    private String description;
}

