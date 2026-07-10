package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class MeRes {
    private String id;
    private String username;
    private String name;
    private String email;
    private String role;
    private String avatar;
    private Boolean isVerified;
    private Date createdAt;
}
