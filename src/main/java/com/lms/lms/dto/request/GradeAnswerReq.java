package com.lms.lms.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GradeAnswerReq {
    @NotNull(message = "Awarded marks are required")
    @DecimalMin(value = "0.0", message = "Awarded marks cannot be negative")
    private BigDecimal awardedMarks;

    private String feedback;
}
