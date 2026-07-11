package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SearchRes {
    private List<SearchItemRes> courses;
    private List<SearchItemRes> webinars;
    private List<SearchItemRes> blogs;
    private Map<String, Long> totals;
    private int page;
    private int limit;
}
