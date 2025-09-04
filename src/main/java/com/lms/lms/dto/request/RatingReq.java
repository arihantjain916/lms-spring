package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RatingReq {
    private String comment;
    @NotBlank(message = "Rating is required")
    private Integer rating;

    @NotBlank(message = "Course ID is required")
    private Long courseId;
}
