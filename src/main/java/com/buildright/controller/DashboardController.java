package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.User;
import com.buildright.repository.*;
import com.buildright.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired private CurrentUser currentUser;
    @Autowired private UserRepository userRepo;
    @Autowired private EquipmentItemRepository itemRepo;
    @Autowired private RentalRepository rentalRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private OperatorRepository operatorRepo;
    @Autowired private DeliveryAgentRepository agentRepo;

    @Autowired private PurchaseRepository purchaseRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        User me = currentUser.get();

        // Role-based dashboard routing
        if (me.isOperator()) return operatorDashboard(model, me);
        if (me.isDriver())   return driverDashboard(model, me);
        if (me.isCustomer()) return customerDashboard(model, me);

        model.addAttribute("currentUser", me);

        // Stats
        model.addAttribute("totalEquipment", itemRepo.countByActive(true));
        model.addAttribute("availableEquipment", itemRepo.countByAvailableQtyGreaterThanAndActive(0, true));
        model.addAttribute("totalCustomers", userRepo.countByRole("CUSTOMER"));
        model.addAttribute("activeRentals",
                rentalRepo.countByStatusIn(List.of("ACTIVE", "APPROVED")));
        model.addAttribute("pendingApprovals",
                rentalRepo.countByStatus("AWAITING_APPROVAL"));
        model.addAttribute("pendingPayments",
                paymentRepo.countByStatus("PENDING"));

        double totalRevenue = paymentRepo.findByStatus("PAID")
                .stream().mapToDouble(p -> p.getTotalAmount()).sum();
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("totalOperators", operatorRepo.count());
        model.addAttribute("availableOperators", operatorRepo.countByAvailable(true));

        // Recent rentals (last 5)
        var recentRentals = rentalRepo.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("recentRentals", recentRentals);

        // Low availability items (< 1 unit left)
        var lowStock = itemRepo.findByAvailableQtyLessThanEqualAndActive(1, true);
        model.addAttribute("lowStockItems", lowStock);

        return "dashboard/index";
    }

    private String operatorDashboard(Model model, User me) {
        operatorRepo.findByLinkedUserId(me.getId()).ifPresent(op -> {
            model.addAttribute("operator", op);
            if (op.getAssignedRentalId() != null && op.getAssignedRentalId() > 0) {
                rentalRepo.findById(op.getAssignedRentalId()).ifPresent(r -> {
                    itemRepo.findById(r.getItemId()).ifPresent(i -> r.setItemName(i.getName()));
                    userRepo.findById(r.getCustomerId()).ifPresent(u -> r.setCustomerName(u.getFullName()));
                    model.addAttribute("assignedRental", r);
                });
            }
        });
        // Job history: all rentals where this operator was assigned
        var allRentals = rentalRepo.findAll().stream()
            .filter(r -> me.getId() != null && operatorRepo.findByLinkedUserId(me.getId())
                .map(op -> op.getId().equals(r.getOperatorId())).orElse(false))
            .peek(r -> {
                itemRepo.findById(r.getItemId()).ifPresent(i -> r.setItemName(i.getName()));
                userRepo.findById(r.getCustomerId()).ifPresent(u -> r.setCustomerName(u.getFullName()));
            }).toList();
        model.addAttribute("jobHistory", allRentals);
        model.addAttribute("currentUser", me);
        return "dashboard/operator";
    }

    private String driverDashboard(Model model, User me) {
        agentRepo.findByLinkedUserId(me.getId()).ifPresent(agent -> {
            model.addAttribute("agent", agent);
            if (agent.getAssignedRentalId() != null && agent.getAssignedRentalId() > 0) {
                rentalRepo.findById(agent.getAssignedRentalId()).ifPresent(r -> {
                    itemRepo.findById(r.getItemId()).ifPresent(i -> r.setItemName(i.getName()));
                    userRepo.findById(r.getCustomerId()).ifPresent(u -> r.setCustomerName(u.getFullName()));
                    model.addAttribute("assignedRental", r);
                });
            }
        });
        var allRentals = rentalRepo.findAll().stream()
            .filter(r -> agentRepo.findByLinkedUserId(me.getId())
                .map(ag -> ag.getId().equals(r.getAgentId())).orElse(false))
            .peek(r -> {
                itemRepo.findById(r.getItemId()).ifPresent(i -> r.setItemName(i.getName()));
                userRepo.findById(r.getCustomerId()).ifPresent(u -> r.setCustomerName(u.getFullName()));
            }).toList();
        model.addAttribute("jobHistory", allRentals);
        model.addAttribute("currentUser", me);
        return "dashboard/driver";
    }

    private String customerDashboard(Model model, User me) {
        var myRentals = rentalRepo.findByCustomerId(me.getId());
        myRentals.forEach(r -> itemRepo.findById(r.getItemId()).ifPresent(i -> r.setItemName(i.getName())));
        var myPurchases = purchaseRepo.findByCustomerId(me.getId());
        myPurchases.forEach(p -> itemRepo.findById(p.getItemId()).ifPresent(i -> {
            p.setItemName(i.getName());
            p.setItemImageUrl(i.getImageUrl());
        }));
        model.addAttribute("myRentals", myRentals);
        model.addAttribute("myPurchases", myPurchases);
        model.addAttribute("activeRentals", myRentals.stream().filter(r -> r.isActiveRental()).count());
        model.addAttribute("pendingPurchases", myPurchases.stream().filter(p -> "PENDING".equals(p.getStatus())).count());
        model.addAttribute("currentUser", me);
        return "dashboard/customer";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
