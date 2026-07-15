package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ContactRes {
    private String id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private String department;
    private String phone;
    private String status;
    private Date createdAt;
}
