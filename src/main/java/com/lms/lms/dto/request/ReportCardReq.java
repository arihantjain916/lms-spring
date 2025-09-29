package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class ReportCardReq {
    private String id;

    @NotNull(message = "Grade is required")
    private String grade;

    @NotNull(message = "Percentage is required")
    private BigDecimal percentage;

    @NotNull(message = "Total Marks is required")
    private BigDecimal totalMarks;

    @NotNull(message = "Obtained is required")
    private BigDecimal obtainedMarks;

    @NotNull(message = "examId is required")
    private String examId;

}
