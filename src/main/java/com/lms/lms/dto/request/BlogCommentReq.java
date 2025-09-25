package com.lms.lms.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlogCommentReq {

    private String id;

    //    @NotBlank(message = "BlogId is required")
    @NotNull(message = "BlogId is required")
    private String blogId;

    //    @NotBlank(message = "Comment is required")
    @NotNull(message = "Comment is required")
    private String comment;
}
