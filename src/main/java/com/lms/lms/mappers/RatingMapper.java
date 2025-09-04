package com.lms.lms.mappers;

import com.lms.lms.dto.response.RatingRes;
import com.lms.lms.modals.Ratings;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    RatingRes toDto(Ratings ratings);
}
