package com.lms.lms.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CategoryReq {
    @NotEmpty(message = "Category Name should not be null")
    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 500, nullable = false)
    @NotEmpty(message = "Category Description should not be null")
    private String description;

    private String id;
}
