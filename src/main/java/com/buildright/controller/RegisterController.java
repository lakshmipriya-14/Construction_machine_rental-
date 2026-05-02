package com.buildright.controller;

import com.buildright.model.User;
import com.buildright.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String contact,
                           @RequestParam String address,
                           RedirectAttributes ra) {

        // Validation
        if (username == null || username.isBlank()) {
            ra.addFlashAttribute("error", "Username is required.");
            return "redirect:/register";
        }
        if (password == null || password.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        if (userRepo.existsByUsername(username.trim())) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken.");
            return "redirect:/register";
        }

        User user = new User(
            username.trim(),
            passwordEncoder.encode(password),
            "CUSTOMER",          // new self-registrations are always CUSTOMER
            fullName,
            email,
            contact,
            address
        );
        userRepo.save(user);

        ra.addFlashAttribute("success", "Account created! You can now sign in.");
        return "redirect:/login?registered=true";
    }
}
