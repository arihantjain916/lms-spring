package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebinarReq {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private String thumbnailUrl;

    private String meetingUrl;

    private String recordingUrl;

    @NotNull(message = "Scheduled date is required")
    private Date scheduledAt;

    private Integer durationMinutes;

    private String categoryId;

    // Optional. Defaults to the authenticated admin when omitted.
    private String hostId;
}
