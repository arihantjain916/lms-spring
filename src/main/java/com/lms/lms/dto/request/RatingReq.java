package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RatingReq {
    private String comment;
    @NotBlank(message = "Rating is required")
    private BigDecimal rating;

    @NotBlank(message = "Course ID is required")
    private Long courseId;
}
