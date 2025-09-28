package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QuestionSubmitReq {
    @NotNull(message = "Question Id is required")
    private String questionId;

    @NotNull(message = "Answer is required")
    private String answer;
}
