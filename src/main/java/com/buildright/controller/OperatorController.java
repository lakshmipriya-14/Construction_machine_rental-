package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.Operator;
import com.buildright.model.User;
import com.buildright.repository.OperatorRepository;
import com.buildright.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/operators")
public class OperatorController {

    @Autowired private OperatorRepository operatorRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CurrentUser currentUser;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model) {
        User me = currentUser.get();
        if (!me.isStaff()) return "redirect:/dashboard";
        model.addAttribute("operators", operatorRepo.findAll());
        model.addAttribute("currentUser", me);
        return "operators/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!currentUser.isStaff()) return "redirect:/operators";
        model.addAttribute("currentUser", currentUser.get());
        return "operators/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name, @RequestParam String specialization,
                         @RequestParam double dailyRate,
                         @RequestParam String username, @RequestParam String password,
                         @RequestParam String contact, @RequestParam String address,
                         RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/operators";
        if (userRepo.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken.");
            return "redirect:/operators/new";
        }
        User u = new User(username, passwordEncoder.encode(password), "OPERATOR",
                name, username + "@buildright.ph", contact, address);
        u = userRepo.save(u);
        operatorRepo.save(new Operator(name, specialization, dailyRate, u.getId()));
        ra.addFlashAttribute("success", "Operator '" + name + "' added.");
        return "redirect:/operators";
    }

    @PostMapping("/{id}/toggle-availability")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/operators";
        operatorRepo.findById(id).ifPresent(op -> {
            if ("IDLE".equals(op.getWorkStatus()) || "UNAVAILABLE".equals(op.getWorkStatus())) {
                op.setAvailable(!op.isAvailable());
                op.setWorkStatus(op.isAvailable() ? "IDLE" : "UNAVAILABLE");
                operatorRepo.save(op);
            }
        });
        ra.addFlashAttribute("success", "Operator availability updated.");
        return "redirect:/operators";
    }
}
