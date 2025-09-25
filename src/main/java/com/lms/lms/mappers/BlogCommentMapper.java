package com.lms.lms.mappers;

import com.lms.lms.dto.response.BlogCommentRes;
import com.lms.lms.modals.BlogComment;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BlogCommentMapper {

    BlogCommentRes toDto(BlogComment comment);
}

