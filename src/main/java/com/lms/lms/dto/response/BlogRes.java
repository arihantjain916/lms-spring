package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogRes {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String read_time;
    private String tag;
    private String imageUrl;
    private UserRes user;
}
