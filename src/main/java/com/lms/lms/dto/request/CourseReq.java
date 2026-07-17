package com.lms.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CourseReq {

    @NotBlank(message = "Course title is required")
    private String title;

    @NotBlank(message = "Course Slug is required")
    private String slug;

    @NotBlank(message = "Course Description is required")
    private String description;

    @NotBlank(message = "Course Category is required")
    private String categoryId;

    private Boolean isFeatured;

    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED|ALL_LEVELS", message = "Level must be one of BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS")
    private String level;

    // Prices the course on create only; later plan changes go through /pricing, and
    // /course/update ignores all three. Omit both for a free course.
    // Attaches an existing reusable plan. Takes precedence over price/currency/planType.
    private String pricingPlanId;

    // Creates a new plan for this course alone.
    @Min(value = 0, message = "Price must be 0 or greater")
    private Double price;

    private String currency;

    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|LIFETIME", message = "Plan type must be one of MONTHLY, QUARTERLY, YEARLY, LIFETIME")
    private String planType;

    private Long id;

}
