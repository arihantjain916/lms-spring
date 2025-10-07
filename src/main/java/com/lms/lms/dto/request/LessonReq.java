package com.lms.lms.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LessonReq {
    private String id;

    @NotNull(message = "Lesson Duration is required")
    private String time;

    @NotNull(message = "Lesson Description is required")
    private String description;

    @NotNull(message = "Lesson Title is required")
    private String title;

    @NotNull(message = "Lesson Video URL is required")
    private String videoUrl;

    @NotNull(message = "Lesson Thumbnail URL is required")
    private String thumbnailUrl;

    @NotNull(message = "Lesson Status is required")
    private String status;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}
