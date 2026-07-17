package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class PricingPlanRes {
    private String id;
    private Long courseId;
    private String title;
    private String description;
    private String currency;
    private Double price;
    private String planType;
    private Date createdAt;
    private Date updatedAt;
}
