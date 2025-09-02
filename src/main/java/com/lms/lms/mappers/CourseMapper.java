package com.lms.lms.mappers;

import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.modals.Category;
import com.lms.lms.modals.Courses;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseRes toDto(Courses courses);
}
