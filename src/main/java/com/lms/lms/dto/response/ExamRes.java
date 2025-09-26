package com.lms.lms.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
public class ExamRes {

    private String id;

    private Boolean shuffleQuestions;
    private Boolean showScoreImmediately;

    private Integer maxAttempts;

    private String title;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private Instant startsAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private Instant endsAt;


    private Integer timeLimitMin;

    private CustomCourseRes course;

    private UserRes user;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private Date createdAt;

}


