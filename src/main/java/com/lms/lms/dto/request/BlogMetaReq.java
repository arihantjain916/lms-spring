package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlogMetaReq {
    // 🔹 SEO (required)
    @NotEmpty(message = "SEO title is required")
    @Size(max = 60, message = "SEO title must be at most 60 characters")
    private String seoTitle;

    @NotEmpty(message = "SEO description is required")
    @Size(max = 160, message = "SEO description must be at most 160 characters")
    private String seoDescription;

    // 🔹 Social (optional)
    @Size(max = 255, message = "OG title must be at most 255 characters")
    private String ogTitle;

    @Size(max = 255, message = "OG description must be at most 255 characters")
    private String ogDescription;

    @Size(max = 255, message = "OG image URL must be at most 255 characters")
    private String ogImageUrl;

    // 🔹 Analytics (optional, defaulted server-side)
    private long views;
    private long likes;
    private long shares;

    // 🔹 SEO flags
    @NotNull(message = "Indexable flag is required")
    private Boolean indexable = true;

    @NotNull(message = "Follow links flag is required")
    private Boolean followLinks = true;

    @NotNull(message = "Featured flag is required")
    private Boolean featured = false;
}
