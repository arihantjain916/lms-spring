package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RatingReq {

    private String id;
    private String comment;
    @NotNull(message = "Rating is required")
    private BigDecimal rating;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}
