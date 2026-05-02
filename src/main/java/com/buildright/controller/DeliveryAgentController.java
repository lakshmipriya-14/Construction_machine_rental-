package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.DeliveryAgent;
import com.buildright.model.User;
import com.buildright.repository.DeliveryAgentRepository;
import com.buildright.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agents")
public class DeliveryAgentController {

    @Autowired private DeliveryAgentRepository agentRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CurrentUser currentUser;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model) {
        User me = currentUser.get();
        if (!me.isStaff()) return "redirect:/dashboard";
        model.addAttribute("agents", agentRepo.findAll());
        model.addAttribute("currentUser", me);
        return "agents/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!currentUser.isStaff()) return "redirect:/agents";
        model.addAttribute("currentUser", currentUser.get());
        return "agents/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name, @RequestParam String vehicleType,
                         @RequestParam String licenseNo, @RequestParam double dailyRate,
                         @RequestParam String username, @RequestParam String password,
                         @RequestParam String contact, @RequestParam String address,
                         RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/agents";
        if (userRepo.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken.");
            return "redirect:/agents/new";
        }
        User u = new User(username, passwordEncoder.encode(password), "DRIVER",
                name, username + "@buildright.ph", contact, address);
        u = userRepo.save(u);
        agentRepo.save(new DeliveryAgent(name, vehicleType, licenseNo, dailyRate, u.getId()));
        ra.addFlashAttribute("success", "Delivery agent '" + name + "' added.");
        return "redirect:/agents";
    }

    @PostMapping("/{id}/toggle-availability")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/agents";
        agentRepo.findById(id).ifPresent(ag -> {
            if ("IDLE".equals(ag.getWorkStatus()) || "UNAVAILABLE".equals(ag.getWorkStatus())) {
                ag.setAvailable(!ag.isAvailable());
                ag.setWorkStatus(ag.isAvailable() ? "IDLE" : "UNAVAILABLE");
                agentRepo.save(ag);
            }
        });
        ra.addFlashAttribute("success", "Agent availability updated.");
        return "redirect:/agents";
    }
}
