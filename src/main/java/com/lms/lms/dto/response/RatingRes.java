package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;


@Getter
@AllArgsConstructor
public class RatingRes {

    private String id;
    private Integer rating;
    private String comment;
    private Date createdAt;
    private Date updatedAt;

    private UserRes user;

}
