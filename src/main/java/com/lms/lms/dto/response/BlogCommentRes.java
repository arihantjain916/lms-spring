package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BlogCommentRes {

    private String id;

    private String comment;

    private List<BlogCommentRes> replies;

    private UserRes user;

}



