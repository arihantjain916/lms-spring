package com.lms.lms.mappers;

import com.lms.lms.dto.response.BlogRes;
import com.lms.lms.modals.Blog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    BlogRes toDto(Blog blog);
}
