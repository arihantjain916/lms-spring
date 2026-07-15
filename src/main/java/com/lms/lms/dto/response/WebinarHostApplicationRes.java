package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class WebinarHostApplicationRes {
    private String id;
    private String name;
    private String email;
    private String topic;
    private String message;
    private String status;
    private UserRes user;
    private Date createdAt;
}
