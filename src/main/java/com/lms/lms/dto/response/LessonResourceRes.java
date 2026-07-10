package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class LessonResourceRes {
    private String id;
    private String title;
    private String type;
    private Date createdAt;
}
