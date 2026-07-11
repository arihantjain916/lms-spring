package com.lms.lms.GlobalValue;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class PublicRoutes {

    public List<String> PUBLIC = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/health",
            "/api/test",
            "/api/contact/**",
            "/api/upload/**",
            "/api/ws/**",
            "/api/webhook/**",
            "/api/payments/webhook",
            "/api/certificates/**"
    );

//    ,

    public List<String> OpenForGet = List.of(
            "/api/course/**",
            "/api/courses",
            "/api/courses/featured",
            "/api/courses/*",
            "/api/courses/*/curriculum",
            "/api/courses/*/instructor",
            "/api/courses/*/reviews",
            "/api/courses/*/related",
            "/api/category/**",
            "/api/ratings/**",
            "/api/blog/**",
            "/api/webhook/**",
            "/api/questions/**"
    );

    public String[] getPublicRoutes() {
        return PUBLIC.toArray(new String[0]);
    }


    public String[] getOpenForGetRoutes() {
        return OpenForGet.toArray(new String[0]);
    }
}



