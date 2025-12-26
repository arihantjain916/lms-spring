package com.lms.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogMetaRes {
    private String id;
    private boolean featured;
    private boolean followLinks;
    private boolean indexable;
    private long likes;

    private String ogDescription;
    private String ogImageUrl;
    private String ogTitle;

    private String seoDescription;
    private String seoTitle;

    private long shares;
    private long views;
}
