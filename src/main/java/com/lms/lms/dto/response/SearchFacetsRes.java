package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SearchFacetsRes {
    private Map<String, Long> types;
    private List<FacetCountRes> categories;
    private List<FacetCountRes> levels;
}
