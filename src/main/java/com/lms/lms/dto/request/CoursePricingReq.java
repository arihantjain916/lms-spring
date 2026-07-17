package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePricingReq {

    // the plans this course should end up on. Anything currently attached and not
    // listed here is detached; an empty list makes the course free.
    @NotNull(message = "Pricing plan ids are required")
    private List<String> pricingPlanIds;
}
