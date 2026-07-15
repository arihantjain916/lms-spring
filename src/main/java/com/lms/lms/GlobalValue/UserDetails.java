package com.lms.lms.GlobalValue;

import com.lms.lms.modals.User;
import com.lms.lms.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UserDetails {

    @Autowired
    private UserRepo userRepo;


    // returns the current user, or null when the request is anonymous / unauthenticated.
    // never casts blindly: on public routes the principal can be the "anonymousUser" String.
    public User userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails)) {
            return null;
        }
        return userRepo.findById(((org.springframework.security.core.userdetails.UserDetails) principal).getUsername()).orElse(null);
    }

    // kept as an explicit alias for call sites that want to signal "anonymous is expected here"
    public User userDetailsOrNull() {
        return this.userDetails();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities();
    }
}
