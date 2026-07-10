package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResourceReq {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Url is required")
    private String url;

    private String type;
}
