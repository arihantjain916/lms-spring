package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class CourseQuestionRes {
    private String id;
    private String content;
    private UserRes user;
    private Integer repliesCount;
    private Integer helpfulCount;
    private Date createdAt;
    private Date updatedAt;
}
