package com.lms.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressReq {

    @NotNull(message = "Watched seconds is required")
    @Min(value = 0, message = "Watched seconds must be at least 0")
    private Integer watchedSeconds;
}
