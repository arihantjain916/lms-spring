package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class EnrollmentDetailsRes {
    private String id;
    private Instant enrolledAt;
    private CourseRes course;
}
