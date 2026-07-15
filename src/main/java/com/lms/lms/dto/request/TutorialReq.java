package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorialReq {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "Content is required")
    private String content;

    private String videoUrl;

    private String thumbnailUrl;

    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED|ALL_LEVELS", message = "Level must be one of BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS")
    private String level;

    private String categoryId;
}
