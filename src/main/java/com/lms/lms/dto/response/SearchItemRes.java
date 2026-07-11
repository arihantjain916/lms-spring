package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchItemRes {
    private String type;
    private String id;
    private String title;
    private String slug;
    private String description;
    private String image;
    private String categoryName;
    private String level;
}
