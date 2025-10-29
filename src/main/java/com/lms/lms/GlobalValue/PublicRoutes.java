package com.lms.lms.GlobalValue;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class PublicRoutes {

    public List<String> PUBLIC = List.of(
            "/api/auth/**", "/api/health", "/api/test", "/api/contact/**", "/api/upload/**"
    );

    public List<String> OpenForGet = List.of(
            "/api/course/**", "/api/category/**", "/api/ratings/**", "/api/blog/**"
    );

    public String[] getPublicRoutes() {
        return PUBLIC.toArray(new String[0]);
    }


    public String[] getOpenForGetRoutes() {
        return OpenForGet.toArray(new String[0]);
    }
}



