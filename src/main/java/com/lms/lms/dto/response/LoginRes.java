package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRes {
    private String message;
    private Boolean status;
    private String token;
}
