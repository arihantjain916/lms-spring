package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class QuestionReplyRes {
    private String id;
    private String content;
    private UserRes user;
    private Date createdAt;
}
