package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ExamSubmitReq {

    @NotNull(message = "Exam Id is required")
    private String examId;

    private List<QuestionSubmitReq> questions;
}
