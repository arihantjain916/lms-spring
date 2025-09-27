package com.lms.lms.dto.request;

import com.lms.lms.modals.Questions;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class QuestionReq {
    private String id;

    @NotNull(message = "Type is required")
    private Questions.Type type;
    @NotNull(message = "Marks is required")
    private BigDecimal marks;
    @NotNull(message = "Title is required")
    private String title;

    private Integer position;
    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "ExamId is required")
    private String examId;
}
