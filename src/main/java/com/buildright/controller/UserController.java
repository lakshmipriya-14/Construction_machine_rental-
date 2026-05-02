package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.User;
import com.buildright.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String role) {
        User me = currentUser.get();
        if (!me.isStaff())
            return "redirect:/dashboard";
        List<User> users = role != null && !role.isBlank()
                ? userRepo.findByRole(role.toUpperCase())
                : userRepo.findAll();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", me);
        model.addAttribute("selectedRole", role);
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!currentUser.isStaff())
            return "redirect:/dashboard";
        model.addAttribute("currentUser", currentUser.get());
        return "users/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String username, @RequestParam String password,
            @RequestParam String role, @RequestParam String fullName,
            @RequestParam String email, @RequestParam String contact,
            @RequestParam String address, RedirectAttributes ra) {
        if (!currentUser.isStaff())
            return "redirect:/dashboard";
        if (userRepo.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Username '" + username + "' already taken.");
            return "redirect:/users/new";
        }
        userRepo.save(new User(username, passwordEncoder.encode(password), role, fullName, email, contact, address));
        ra.addFlashAttribute("success", "User '" + fullName + "' created.");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User me = currentUser.get();
        if (!me.isStaff() && !me.getId().equals(id))
            return "redirect:/dashboard";
        User u = userRepo.findById(id).orElse(null);
        if (u == null)
            return "redirect:/users";
        model.addAttribute("user", u);
        model.addAttribute("currentUser", me);
        return "users/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam String fullName,
            @RequestParam String email, @RequestParam String contact,
            @RequestParam String address,
            @RequestParam(required = false) String password,
            RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isStaff() && !me.getId().equals(id))
            return "redirect:/dashboard";
        userRepo.findById(id).ifPresent(u -> {
            u.setFullName(fullName);
            u.setEmail(email);
            u.setContact(contact);
            u.setAddress(address);
            if (password != null && !password.isBlank())
                u.setPassword(passwordEncoder.encode(password));
            userRepo.save(u);
        });
        ra.addFlashAttribute("success", "Profile updated.");
        return me.isStaff() ? "redirect:/users" : "redirect:/dashboard";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isOwner()) {
            ra.addFlashAttribute("error", "Only Admins can deactivate or reactivate accounts.");
            return "redirect:/users";
        }
        userRepo.findById(id).ifPresent(u -> {
            u.setActive(!u.isActive());
            userRepo.save(u);
        });
        ra.addFlashAttribute("success", "User status toggled.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isOwner()) {
            ra.addFlashAttribute("error", "Only Admins can delete accounts.");
            return "redirect:/users";
        }
        userRepo.deleteById(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/users";
    }
}