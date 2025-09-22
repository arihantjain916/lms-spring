package com.lms.lms.GlobalValue;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class PublicRoutes {

    public List<String> PUBLIC = List.of(
            "/auth/**", "/health", "/test", "/contact/**", "/upload/**",
            "/course/**", "/category/**", "/ratings/**", "/blog/**"
    );

    public String[] getPublicRoutes() {
        return PUBLIC.toArray(new String[0]);
    }
}



