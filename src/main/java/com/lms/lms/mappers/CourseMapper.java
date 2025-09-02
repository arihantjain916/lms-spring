package com.lms.lms.mappers;

import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.modals.Courses;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseRes toDto(Courses courses);
}
