package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class PricingPlanRes {
    private String id;
    // every course this plan is attached to; a plan is reusable across courses
    private List<Long> courseIds;
    private String title;
    private String description;
    private String currency;
    private Double price;
    private String planType;
    private Date createdAt;
    private Date updatedAt;
}
