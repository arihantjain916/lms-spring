package com.lms.lms.dto.request;

import com.lms.lms.modals.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BroadcastReq {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Body cannot exceed 2000 characters")
    private String body;

    // frontend route the notification opens; optional
    private String link;

    // null targets every active user; otherwise only this role
    private User.Role role;
}
