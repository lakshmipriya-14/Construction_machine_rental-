package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.User;
import com.buildright.repository.DeliveryAgentRepository;
import com.buildright.repository.OperatorRepository;
import com.buildright.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WorkStatusController {

    @Autowired private CurrentUser currentUser;
    @Autowired private OperatorRepository operatorRepo;
    @Autowired private DeliveryAgentRepository agentRepo;
    @Autowired private RentalRepository rentalRepo;

    // ── OPERATOR: mark job done ───────────────────────────────────
    @PostMapping("/operator/job-done")
    public String operatorJobDone(RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isOperator()) return "redirect:/dashboard";
        operatorRepo.findByLinkedUserId(me.getId()).ifPresent(op -> {
            op.markJobDone();
            operatorRepo.save(op);
            // Also update rental status
            if (op.getAssignedRentalId() > 0) {
                rentalRepo.findById(op.getAssignedRentalId()).ifPresent(r -> {
                    if ("ACTIVE".equals(r.getStatus())) {
                        r.addAudit("Operator " + op.getName() + " marked job as done.");
                        rentalRepo.save(r);
                    }
                });
            }
        });
        ra.addFlashAttribute("success", "Job marked as done! Waiting for admin confirmation.");
        return "redirect:/dashboard";
    }

    // ── OPERATOR: toggle availability ─────────────────────────────
    @PostMapping("/operator/toggle-availability")
    public String operatorToggleAvailability(RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isOperator()) return "redirect:/dashboard";
        operatorRepo.findByLinkedUserId(me.getId()).ifPresent(op -> {
            if ("ON_DUTY".equals(op.getWorkStatus())) {
                ra.addFlashAttribute("error", "Cannot change availability while on an active job.");
                return;
            }
            boolean newAvailability = !op.isAvailable();
            op.setAvailable(newAvailability);
            op.setWorkStatus(newAvailability ? "IDLE" : "UNAVAILABLE");
            operatorRepo.save(op);
        });
        ra.addFlashAttribute("success", "Availability updated.");
        return "redirect:/dashboard";
    }

    // ── DRIVER: mark delivery done ────────────────────────────────
    @PostMapping("/driver/delivery-done")
    public String driverDeliveryDone(RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isDriver()) return "redirect:/dashboard";
        agentRepo.findByLinkedUserId(me.getId()).ifPresent(agent -> {
            agent.markDelivered();
            agentRepo.save(agent);
            if (agent.getAssignedRentalId() > 0) {
                rentalRepo.findById(agent.getAssignedRentalId()).ifPresent(r -> {
                    r.addAudit("Driver " + agent.getName() + " marked delivery as done.");
                    rentalRepo.save(r);
                });
            }
        });
        ra.addFlashAttribute("success", "Delivery marked as done! Waiting for admin confirmation.");
        return "redirect:/dashboard";
    }

    // ── DRIVER: toggle availability ───────────────────────────────
    @PostMapping("/driver/toggle-availability")
    public String driverToggleAvailability(RedirectAttributes ra) {
        User me = currentUser.get();
        if (!me.isDriver()) return "redirect:/dashboard";
        agentRepo.findByLinkedUserId(me.getId()).ifPresent(agent -> {
            if ("ON_DELIVERY".equals(agent.getWorkStatus())) {
                ra.addFlashAttribute("error", "Cannot change availability while on an active delivery.");
                return;
            }
            boolean newAvailability = !agent.isAvailable();
            agent.setAvailable(newAvailability);
            agent.setWorkStatus(newAvailability ? "IDLE" : "UNAVAILABLE");
            agentRepo.save(agent);
        });
        ra.addFlashAttribute("success", "Availability updated.");
        return "redirect:/dashboard";
    }
}
