package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class OrderRes {
    private String id;
    private Long courseId;
    private String courseTitle;
    private String planId;
    private String planTitle;
    private Double amount;
    private String currency;
    private String status;
    private String paymentReference;
    private Date createdAt;
}
