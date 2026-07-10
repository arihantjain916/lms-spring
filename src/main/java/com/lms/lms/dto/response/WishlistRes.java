package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class WishlistRes {
    private String id;
    private Instant addedAt;
    private CourseRes course;
}
