package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class TutorialRes {
    private String id;
    private String title;
    private String slug;
    private String description;
    private String content;
    private String videoUrl;
    private String thumbnailUrl;
    private String level;
    private String categoryId;
    private String categoryName;
    private UserRes author;
    private Date createdAt;
    private Date updatedAt;
}
