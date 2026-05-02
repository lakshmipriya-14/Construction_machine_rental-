package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.*;
import com.buildright.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired private PaymentRepository paymentRepo;
    @Autowired private PurchaseRepository purchaseRepo;
    @Autowired private RentalRepository rentalRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private EquipmentItemRepository itemRepo;
    @Autowired private CurrentUser currentUser;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false, defaultValue = "all") String type) {
        User me = currentUser.get();
        if (!me.isStaff()) return "redirect:/dashboard";

        List<Payment> payments = status != null && !status.isBlank()
                ? paymentRepo.findByStatus(status.toUpperCase())
                : paymentRepo.findAll();

        payments.forEach(p -> {
            rentalRepo.findById(p.getRentalId()).ifPresent(r -> {
                userRepo.findById(r.getCustomerId()).ifPresent(u -> p.setCustomerName(u.getFullName()));
                itemRepo.findById(r.getItemId()).ifPresent(i -> p.setItemName(i.getName()));
            });
        });

        // Purchase payments for "for sale" tab
        List<Purchase> purchases = purchaseRepo.findAll();
        purchases.forEach(p -> {
            userRepo.findById(p.getCustomerId()).ifPresent(u -> p.setCustomerName(u.getFullName()));
            itemRepo.findById(p.getItemId()).ifPresent(i -> {
                p.setItemName(i.getName());
                p.setItemImageUrl(i.getImageUrl());
            });
        });

        double totalPaid = paymentRepo.findByStatus("PAID").stream().mapToDouble(Payment::getTotalAmount).sum();
        double totalSales = purchaseRepo.findByStatus("CONFIRMED").stream().mapToDouble(Purchase::getTotalAmount).sum();

        model.addAttribute("payments", payments);
        model.addAttribute("purchases", purchases);
        model.addAttribute("currentUser", me);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("totalRevenue", totalPaid);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("pendingCount", paymentRepo.countByStatus("PENDING"));
        model.addAttribute("refundedCount", paymentRepo.countByStatus("REFUNDED"));
        return "payments/list";
    }

    // ── Excel download for rental payments ─────────────────────────
    @GetMapping("/export/rental")
    public void exportRental(HttpServletResponse response) throws IOException {
        if (!currentUser.isStaff()) { response.sendRedirect("/payments"); return; }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=rental_payments.csv");
        PrintWriter w = response.getWriter();
        w.println("Receipt No,Rental No,Customer,Equipment,Base Amount,Operator Fee,Delivery Fee,Total,Method,Reference,Date,Status");
        paymentRepo.findAll().forEach(p -> {
            String[] cust = {""}, item = {""};
            rentalRepo.findById(p.getRentalId()).ifPresent(r -> {
                userRepo.findById(r.getCustomerId()).ifPresent(u -> cust[0] = u.getFullName());
                itemRepo.findById(r.getItemId()).ifPresent(i -> item[0] = i.getName());
            });
            w.printf("RCP-%d,%d,\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%s,%s,%s,%s%n",
                    p.getId(), p.getRentalId(), cust[0], item[0],
                    p.getBaseAmount(), p.getOperatorFee(), p.getDeliveryFee(), p.getTotalAmount(),
                    p.getMethod(), p.getReferenceNo(), p.getPaymentDate(), p.getStatus());
        });
    }

    // ── Excel download for purchase payments ───────────────────────
    @GetMapping("/export/purchase")
    public void exportPurchase(HttpServletResponse response) throws IOException {
        if (!currentUser.isStaff()) { response.sendRedirect("/payments"); return; }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=purchase_payments.csv");
        PrintWriter w = response.getWriter();
        w.println("Order No,Customer,Tool,Qty,Unit Price,Total,Method,Reference,Date,Status,Delivery Address");
        purchaseRepo.findAll().forEach(p -> {
            String[] cust = {""}, item = {""};
            userRepo.findById(p.getCustomerId()).ifPresent(u -> cust[0] = u.getFullName());
            itemRepo.findById(p.getItemId()).ifPresent(i -> item[0] = i.getName());
            w.printf("ORD-%d,\"%s\",\"%s\",%d,%.2f,%.2f,%s,%s,%s,%s,\"%s\"%n",
                    p.getId(), cust[0], item[0], p.getQuantity(),
                    p.getUnitPrice(), p.getTotalAmount(),
                    p.getPaymentMethod(), p.getReferenceNo(), p.getPaymentDate(),
                    p.getStatus(), p.getDeliveryAddress());
        });
    }
}
