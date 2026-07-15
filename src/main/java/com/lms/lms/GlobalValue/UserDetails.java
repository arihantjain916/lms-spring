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


    public User userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.UserDetails user = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        return userRepo.findById(user.getUsername()).orElse(null);
    }

    // safe variant for public endpoints: returns null for anonymous / unauthenticated requests
    public User userDetailsOrNull() {
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

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities();
    }
}
