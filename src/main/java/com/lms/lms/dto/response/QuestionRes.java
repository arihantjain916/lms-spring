package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public class QuestionRes {
    private String id;
    private String type;
    private String marks;
    private String title;
    private String description;

    private List<QuestionOptionRes> options;
}
