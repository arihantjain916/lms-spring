package com.lms.lms.mappers;

import com.lms.lms.dto.response.ReportCardRes;
import com.lms.lms.modals.ReportCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportCardRes toDto(ReportCard reportCard);
}
