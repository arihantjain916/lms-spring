package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LearningCourseRes {
    private CourseRes course;
    private CourseProgressRes progress;
    private List<LessonProgressRes> lessons;
}
