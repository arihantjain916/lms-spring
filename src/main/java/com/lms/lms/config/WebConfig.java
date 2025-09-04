package com.lms.lms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /Uploads/** to the local Uploads folder
        registry.addResourceHandler("/Uploads/**")
                .addResourceLocations("file:./Uploads/");
    }
}
