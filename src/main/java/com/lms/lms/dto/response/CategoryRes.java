package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRes {
    private String id;
    private String name;
    private String description;
    private long courseCount;
}
