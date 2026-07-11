package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class WebinarRegistrationRes {
    private String id;
    private Date registeredAt;
    private WebinarRes webinar;
}
