package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QuestionRes {
    private String id;
    private String type;
    private String marks;
    private String title;
    private String description;
}
