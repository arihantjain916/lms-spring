package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class AdminUserRes {
    private String id;
    private String name;
    private String username;
    private String email;
    private String role;
    private String avatar;
    private Boolean isVerified;
    private Boolean isBanned;
    private Boolean isActive;
    private Boolean isDeleted;
    private Date createdAt;
}
