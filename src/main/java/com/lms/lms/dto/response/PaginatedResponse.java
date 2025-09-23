package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private String message;
    private Boolean status;
    private List<T> data;
    private int currentPage;
    private int size;
    private long totalElements;
    private int totalPages;
}
