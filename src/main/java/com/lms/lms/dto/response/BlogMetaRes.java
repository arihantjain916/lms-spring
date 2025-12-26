package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class BlogMetaRes {
    private String id;
    private Boolean featured;
    private Boolean follow_links;
    private Boolean indexable;
    private BigInteger likes;

    private String og_description;
    private String og_image_url;
    private String og_title;

    private String seo_description;
    private String seo_title;

    private BigInteger shares;
    private BigInteger views;
}
