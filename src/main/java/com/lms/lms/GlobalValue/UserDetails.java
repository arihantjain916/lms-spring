package com.lms.lms.GlobalValue;

import com.lms.lms.modals.User;
import com.lms.lms.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserDetails {

    @Autowired
    private UserRepo userRepo;

    public User userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.UserDetails user = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        return userRepo.findById(user.getUsername()).orElse(null);
    }
}
