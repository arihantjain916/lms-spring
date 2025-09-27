package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionReq {
    private String id;

    @NotNull(message = "IsCorrect is required")
    private Boolean isCorrect;

    @NotNull(message = "Option is required")
    private String option;
}
