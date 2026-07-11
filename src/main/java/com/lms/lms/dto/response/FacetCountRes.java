package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacetCountRes {
    private String id;
    private String name;
    private Long count;
}
