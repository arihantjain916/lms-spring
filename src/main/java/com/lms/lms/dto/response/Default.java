package com.lms.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
//@NoArgsConstructor
@Getter
public class Default {

    private String message;
    private Boolean status;

    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String date;
}
