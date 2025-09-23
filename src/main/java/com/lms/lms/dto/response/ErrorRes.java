package com.lms.lms.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorRes {
    private String timestamp;
    private int status;
    private String error;
    private String message;
}
