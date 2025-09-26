package com.lms.lms.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamRes {

    private String id;

    private Boolean shuffleQuestions;
    private Boolean showScoreImmediately;

    private Integer maxAttempts;

    private String title;


    private String startsAt;


    private String endsAt;


    private Integer timeLimitMin;

    private CustomCourseRes course;

    private UserRes user;

}


