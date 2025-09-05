package com.lms.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CourseRes {
    private Long id;
    private String slug;
    private String title;
    private String description;

    private Double price;

    private UserRes user;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.0")
    private Double avgRating;

    private Integer totalRating;

    private CustomCategoryRes category;

    private Integer upvote;
    private Integer downvote;
}

