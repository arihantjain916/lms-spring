package com.lms.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PricingPlanReq {

    @NotBlank(message = "Plan title is required")
    private String title;

    @NotBlank(message = "Plan description is required")
    private String description;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or greater")
    private Double price;

    @NotBlank(message = "Plan type is required")
    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|LIFETIME", message = "Plan type must be one of MONTHLY, QUARTERLY, YEARLY, LIFETIME")
    private String planType;
}
