package com.buildright.config;

import com.buildright.model.User;
import com.buildright.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    @Autowired private UserRepository userRepo;

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }

    public boolean isStaff() {
        User u = get();
        return u != null && (u.isOwner() || u.isManager());
    }

    public boolean isOwner() {
        User u = get();
        return u != null && u.isOwner();
    }

    public boolean isCustomer() {
        User u = get();
        return u != null && u.isCustomer();
    }
}
