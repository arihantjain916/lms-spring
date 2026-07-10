package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class CertificateRes {
    private String id;
    private String certificateNumber;
    private String userName;
    private String courseTitle;
    private Date issuedAt;
}
