package com.lms.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
//@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Default {

    private String message;
    private Boolean status;

    @Nullable
    private String date;


    @Nullable
    private Object data;

//    @Nullable
//    private Object error;
//
//
//    public Default(String message, Boolean status, @Nullable String date, @Nullable Object data) {
//        this.message = message;
//        this.status = status;
//        this.date = date;
//        this.data = data;
//        this.error = null;
//    }
}
