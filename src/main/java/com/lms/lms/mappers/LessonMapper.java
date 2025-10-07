package com.lms.lms.mappers;

import com.lms.lms.dto.response.LessonRes;
import com.lms.lms.modals.Lesson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    LessonRes toDto(Lesson lesson);
}
