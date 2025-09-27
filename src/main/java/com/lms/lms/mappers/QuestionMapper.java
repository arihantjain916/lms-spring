package com.lms.lms.mappers;


import com.lms.lms.dto.response.QuestionRes;
import com.lms.lms.modals.Questions;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionRes toDto(Questions questions);
}
