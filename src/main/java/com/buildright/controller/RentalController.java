package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.*;
import com.buildright.repository.*;
import com.buildright.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    @Autowired private RentalService rentalService;
    @Autowired private RentalRepository rentalRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private EquipmentItemRepository itemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private OperatorRepository operatorRepo;
    @Autowired private DeliveryAgentRepository agentRepo;
    @Autowired private CurrentUser currentUser;

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String status) {
        User me = currentUser.get();
        List<Rental> rentals;
        if (me.isCustomer()) {
            rentals = rentalService.getByCustomerEnriched(me.getId());
        } else {
            rentals = rentalService.getAllEnriched();
        }
        if (status != null && !status.isBlank()) {
            rentals = rentals.stream().filter(r -> r.getStatus().equals(status.toUpperCase())).toList();
        }
        model.addAttribute("rentals", rentals);
        model.addAttribute("currentUser", me);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingExtensions",
                rentals.stream().filter(r -> "PENDING".equals(r.getExtensionStatus())).count());
        return "rentals/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        User me = currentUser.get();
        if (!me.isCustomer() && !me.isStaff()) return "redirect:/rentals";
        model.addAttribute("items", itemRepo.findByActive(true));
        model.addAttribute("currentUser", me);
        model.addAttribute("allOperators", operatorRepo.findAll());
        model.addAttribute("allAgents", agentRepo.findAll());
        if (me.isStaff()) {
            model.addAttribute("customers", userRepo.findByRole("CUSTOMER"));
        }
        return "rentals/new";
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long customerId,
                         @RequestParam Long itemId,
                         @RequestParam int quantity,
                         @RequestParam String startDate,
                         @RequestParam String endDate,
                         @RequestParam(defaultValue = "false") boolean needsOperator,
                         @RequestParam(defaultValue = "false") boolean needsDelivery,
                         @RequestParam(defaultValue = "") String deliveryAddress,
                         RedirectAttributes ra) {
        User me = currentUser.get();
        Long custId = me.isCustomer() ? me.getId() : customerId;
        try {
            Rental r = rentalService.createRental(custId, itemId, quantity, startDate, endDate,
                    needsOperator, needsDelivery, deliveryAddress);
            ra.addFlashAttribute("success",
                    "Rental #" + r.getId() + " created! Please complete payment to proceed.");
            return "redirect:/rentals/" + r.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Rental r = rentalRepo.findById(id).orElse(null);
        if (r == null) return "redirect:/rentals";
        User me = currentUser.get();
        if (me.isCustomer() && !r.getCustomerId().equals(me.getId())) return "redirect:/rentals";

        rentalService.enrich(r);
        Payment payment = paymentRepo.findByRentalId(id).orElse(null);
        model.addAttribute("rental", r);
        model.addAttribute("payment", payment);
        model.addAttribute("currentUser", me);
        model.addAttribute("auditEntries", r.getAuditEntries());
        return "rentals/detail";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id,
                      @RequestParam String method,
                      @RequestParam String referenceNo,
                      @RequestParam String paymentDate,
                      RedirectAttributes ra) {
        try {
            rentalService.processPayment(id, method, referenceNo, paymentDate);
            ra.addFlashAttribute("success", "Payment recorded. Awaiting manager approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(defaultValue = "") String note,
                          RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.approveRental(id, note);
            ra.addFlashAttribute("success", "Rental #" + id + " approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(defaultValue = "") String reason,
                         RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.rejectRental(id, reason);
            ra.addFlashAttribute("success", "Rental #" + id + " rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.activateRental(id);
            ra.addFlashAttribute("success", "Rental #" + id + " is now ACTIVE.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/return")
    public String processReturn(@PathVariable Long id,
                                @RequestParam(defaultValue = "") String remarks,
                                RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.processReturn(id, remarks);
            ra.addFlashAttribute("success", "Rental #" + id + " returned successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        User me = currentUser.get();
        try {
            rentalService.cancelRental(id, me.getId());
            ra.addFlashAttribute("success", "Rental #" + id + " cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/extend-request")
    public String requestExtension(@PathVariable Long id,
                                   @RequestParam String newEndDate,
                                   RedirectAttributes ra) {
        User me = currentUser.get();
        try {
            rentalService.requestExtension(id, me.getId(), newEndDate);
            ra.addFlashAttribute("success", "Extension request submitted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/extend-approve")
    public String approveExtension(@PathVariable Long id, RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.approveExtension(id);
            ra.addFlashAttribute("success", "Extension approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/extend-reject")
    public String rejectExtension(@PathVariable Long id,
                                  @RequestParam(defaultValue = "") String reason,
                                  RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/rentals";
        try {
            rentalService.rejectExtension(id, reason);
            ra.addFlashAttribute("success", "Extension rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/confirm-done")
    public String confirmDone(@PathVariable Long id, RedirectAttributes ra) {
        User me = currentUser.get();
        try {
            rentalService.customerConfirmDone(id, me.getId());
            ra.addFlashAttribute("success", "Confirmed! The manager will finalize the return.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        Rental r = rentalRepo.findById(id).orElse(null);
        if (r == null) return "redirect:/rentals";
        User me = currentUser.get();
        if (me.isCustomer() && !r.getCustomerId().equals(me.getId())) return "redirect:/rentals";
        rentalService.enrich(r);
        Payment payment = paymentRepo.findByRentalId(id).orElse(null);
        if (payment == null || !"PAID".equals(payment.getStatus())) return "redirect:/rentals/" + id;
        userRepo.findById(r.getCustomerId()).ifPresent(u -> model.addAttribute("customer", u));
        model.addAttribute("rental", r);
        model.addAttribute("payment", payment);
        model.addAttribute("currentUser", me);
        return "rentals/receipt";
    }

    @GetMapping("/report")
    public String report(Model model,
                         @RequestParam(required = false) String status) {
        if (!currentUser.isStaff()) return "redirect:/dashboard";
        User me = currentUser.get();
        var all = rentalService.getAllEnriched();
        if (status != null && !status.isBlank())
            all = all.stream().filter(r -> r.getStatus().equals(status.toUpperCase())).toList();
        double totalRevenue = paymentRepo.findByStatus("PAID").stream()
                .mapToDouble(Payment::getTotalAmount).sum();
        long activeCount   = rentalRepo.countByStatusIn(List.of("ACTIVE","APPROVED"));
        long pendingCount  = rentalRepo.countByStatus("AWAITING_APPROVAL");
        long returnedCount = rentalRepo.countByStatus("RETURNED");
        model.addAttribute("rentals", all);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("totalRentals", all.size());
        model.addAttribute("currentUser", me);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("generatedAt",
            java.time.LocalDateTime.now().toString().replace("T"," ").substring(0,19));
        return "rentals/report";
    }
}
