// src/main/java/com/acme/lms/security/ViewerIdResolver.java
package com.lms.lms.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ViewerIdResolver {

    public Viewer currentViewer(String guestIdCookie) {
        // If we already issued a guest cookie, reuse it
        String guestId = (guestIdCookie != null && !guestIdCookie.isBlank())
                ? guestIdCookie
                : UUID.randomUUID().toString();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || isAnonymous(auth)) {
            return new Viewer("guest:" + guestId, false);
        }
        UserDetails user = (UserDetails) auth.getPrincipal();
        return new Viewer("user:" + user.getUsername(), true);
//
//        // JWT
//        if (auth instanceof JwtAuthenticationToken jwtAuth) {
//            String userId = jwtAuth.getToken().getClaimAsString("sub");
//            if (userId == null) userId = jwtAuth.getToken().getClaimAsString("user_id");
//            if (userId != null) return new Viewer(userId, true);
//        }
//
//        // UserDetails
//        Object principal = auth.getPrincipal();
//        if (principal instanceof UserDetails ud) {
//            return new Viewer(ud.getUsername(), true);
//        }
//
//        // Fallback
//        return new Viewer(guestId, false);
    }

    private boolean isAnonymous(Authentication auth) {
        return auth.getAuthorities() != null &&
                auth.getAuthorities().stream().anyMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
    }

    public record Viewer(String id, boolean authenticated) {
    }
}
