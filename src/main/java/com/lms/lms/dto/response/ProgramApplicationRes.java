package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ProgramApplicationRes {
    private String id;
    private String programId;
    private String name;
    private String email;
    private String phone;
    private String message;
    private String status;
    private Date createdAt;
}
