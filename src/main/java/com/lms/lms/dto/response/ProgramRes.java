package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ProgramRes {
    private String id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private Integer durationWeeks;
    private Date startDate;
    private Double price;
    private String currency;
    private Boolean isActive;
    private Date createdAt;
}
