package com.lms.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BlogRes {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String description;
    private String status;
    private String Category;
    private Boolean isFeatured;
    private String read_time;
    private String tag;
    private String imageUrl;
    private UserRes user;
    private BlogMetaRes blogMeta;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM, yyyy")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM, yyyy")
    private LocalDateTime updatedAt;
}
