package com.lms.lms.service;

import com.lms.lms.modals.User;
import com.lms.lms.modals.UserPrincipal;
import com.lms.lms.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       try{
           User user = userRepo.findByUsername(username).orElse(null);
           if (user == null) {
               throw new UsernameNotFoundException("User Not Found");
           }
           return new UserPrincipal(user);
       } catch (Exception e) {
           throw  new UsernameNotFoundException("User not found");
       }
    }

    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        try{
            User user = userRepo.findById(id).orElse(null);
            if (user == null) {
                throw new UsernameNotFoundException("User Not Found");
            }
            return new UserPrincipal(user);
        } catch (Exception e) {
            throw  new UsernameNotFoundException("User not found");
        }
    }
}
