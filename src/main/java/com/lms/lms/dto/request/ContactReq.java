package com.lms.lms.dto.request;

import com.lms.lms.modals.ContactUs;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ContactReq {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @Column(nullable = false)
    @NotNull(message = "Department is required")
    private ContactUs.Department department;

    @NotBlank(message = "Phone is required")
    private String phone;

}
