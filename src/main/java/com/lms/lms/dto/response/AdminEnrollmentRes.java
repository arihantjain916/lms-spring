package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AdminEnrollmentRes {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private Long courseId;
    private String courseTitle;
    private Instant enrolledAt;
}
