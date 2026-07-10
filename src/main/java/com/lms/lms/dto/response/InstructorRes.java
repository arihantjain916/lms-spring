package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InstructorRes {
    private String id;
    private String username;
    private String name;
    private String avatar;
    private Integer totalCourses;
}
