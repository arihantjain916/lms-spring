package com.lms.lms.mappers;

import com.lms.lms.dto.response.ExamRes;
import com.lms.lms.modals.Exam;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExamMapper {
    ExamRes toDto(Exam exam);
}



