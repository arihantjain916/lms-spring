package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BlogCommentRes {

    private String id;

    private String comment;

    private LocalDateTime createdAt;

    private List<BlogCommentRes> replies;

    private UserRes user;

}



