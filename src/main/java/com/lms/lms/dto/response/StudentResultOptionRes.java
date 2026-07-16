package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentResultOptionRes {

    private String option;

    // whether the learner picked this option
    private boolean selected;

    // whether this option is the right one; safe to expose because results are only
    // ever returned for a completed, graded attempt
    private boolean correct;
}
