package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.*;
import com.buildright.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PurchaseController {

    @Autowired private PurchaseRepository purchaseRepo;
    @Autowired private EquipmentItemRepository itemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CurrentUser currentUser;

    // ── SHOP PAGE: all tools for sale ─────────────────────────────
    @GetMapping("/shop")
    public String shop(Model model) {
        List<EquipmentItem> tools = itemRepo.findAll().stream()
                .filter(i -> i.isForSale() && i.isTool() && i.isActive() && i.getAvailableQty() > 0)
                .toList();
        model.addAttribute("tools", tools);
        model.addAttribute("currentUser", currentUser.get());
        return "shop/list";
    }

    // ── BUY FORM: shown when customer clicks Buy Now ──────────────
    @GetMapping("/shop/{id}/buy")
    public String buyForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        EquipmentItem item = itemRepo.findById(id).orElse(null);
        if (item == null || !item.isForSale() || !item.isTool() || !item.isActive()) {
            ra.addFlashAttribute("error", "Item not available for purchase.");
            return "redirect:/shop";
        }
        model.addAttribute("item", item);
        model.addAttribute("currentUser", currentUser.get());
        return "shop/buy";
    }

    // ── SUBMIT PURCHASE ───────────────────────────────────────────
    @PostMapping("/shop/{id}/buy")
    public String submitBuy(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam String deliveryAddress,
                            @RequestParam(required = false) String notes,
                            @RequestParam String paymentMethod,
                            @RequestParam(required = false, defaultValue = "") String referenceNo,
                            @RequestParam String paymentDate,
                            RedirectAttributes ra) {
        User me = currentUser.get();
        EquipmentItem item = itemRepo.findById(id).orElse(null);

        if (item == null || !item.isForSale() || !item.isActive()) {
            ra.addFlashAttribute("error", "Item not available.");
            return "redirect:/shop";
        }
        if (quantity < 1) {
            ra.addFlashAttribute("error", "Quantity must be at least 1.");
            return "redirect:/shop/" + id + "/buy";
        }
        if (quantity > item.getAvailableQty()) {
            ra.addFlashAttribute("error", "Only " + item.getAvailableQty() + " unit(s) in stock.");
            return "redirect:/shop/" + id + "/buy";
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            ra.addFlashAttribute("error", "Delivery address is required.");
            return "redirect:/shop/" + id + "/buy";
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            ra.addFlashAttribute("error", "Please select a payment method.");
            return "redirect:/shop/" + id + "/buy";
        }
        if ((paymentMethod.equals("GCASH") || paymentMethod.equals("BANK_TRANSFER"))
                && (referenceNo == null || referenceNo.isBlank())) {
            ra.addFlashAttribute("error", "Reference number is required for " + paymentMethod + ".");
            return "redirect:/shop/" + id + "/buy";
        }

        // Deduct stock
        item.setAvailableQty(item.getAvailableQty() - quantity);
        itemRepo.save(item);

        // Create and immediately confirm the purchase with payment details
        Purchase purchase = new Purchase(me.getId(), item.getId(), quantity,
                item.getSalePrice(), deliveryAddress.trim(),
                notes != null ? notes.trim() : "");
        purchase.confirm(paymentMethod, referenceNo, paymentDate);
        purchaseRepo.save(purchase);

        ra.addFlashAttribute("success", "Order placed and payment recorded!");
        return "redirect:/purchases/" + purchase.getId() + "/receipt";
    }

    // ── PURCHASE RECEIPT ──────────────────────────────────────────
    @GetMapping("/purchases/{id}/receipt")
    public String purchaseReceipt(@PathVariable Long id, Model model) {
        User me = currentUser.get();
        Purchase purchase = purchaseRepo.findById(id).orElse(null);
        if (purchase == null) return "redirect:/purchases";
        if (!me.isStaff() && !purchase.getCustomerId().equals(me.getId())) return "redirect:/purchases";

        itemRepo.findById(purchase.getItemId()).ifPresent(i -> {
            purchase.setItemName(i.getName());
            purchase.setItemImageUrl(i.getImageUrl());
        });
        userRepo.findById(purchase.getCustomerId()).ifPresent(u -> {
            purchase.setCustomerName(u.getFullName());
            model.addAttribute("customer", u);
        });
        model.addAttribute("purchase", purchase);
        model.addAttribute("currentUser", me);
        return "shop/purchase_receipt";
    }

    // ── MY PURCHASES (customer view) ──────────────────────────────
    @GetMapping("/purchases")
    public String myPurchases(Model model) {
        User me = currentUser.get();
        List<Purchase> purchases;

        if (me.isStaff()) {
            purchases = purchaseRepo.findAll();
        } else {
            purchases = purchaseRepo.findByCustomerId(me.getId());
        }

        // Enrich with names and images
        purchases.forEach(p -> {
            userRepo.findById(p.getCustomerId()).ifPresent(u -> p.setCustomerName(u.getFullName()));
            itemRepo.findById(p.getItemId()).ifPresent(i -> {
                p.setItemName(i.getName());
                p.setItemImageUrl(i.getImageUrl());
            });
        });

        model.addAttribute("purchases", purchases);
        model.addAttribute("currentUser", me);

        if (me.isStaff()) {
            model.addAttribute("pendingCount", purchaseRepo.countByStatus("PENDING"));
            model.addAttribute("confirmedCount", purchaseRepo.countByStatus("CONFIRMED"));
        }
        return "shop/purchases";
    }

    // ── CONFIRM PURCHASE (staff only) ─────────────────────────────
    @PostMapping("/purchases/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          @RequestParam String paymentMethod,
                          @RequestParam(required = false) String referenceNo,
                          @RequestParam(required = false) String paymentDate,
                          RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/purchases";
        purchaseRepo.findById(id).ifPresent(p -> {
            p.confirm(paymentMethod, referenceNo != null ? referenceNo : "", paymentDate != null ? paymentDate : "");
            purchaseRepo.save(p);
        });
        ra.addFlashAttribute("success", "Order #" + id + " confirmed.");
        return "redirect:/purchases";
    }

    // ── CANCEL PURCHASE ───────────────────────────────────────────
    @PostMapping("/purchases/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        User me = currentUser.get();
        purchaseRepo.findById(id).ifPresent(p -> {
            // Customer can cancel their own PENDING orders; staff can cancel any
            boolean canCancel = me.isStaff() || (p.getCustomerId().equals(me.getId()) && "PENDING".equals(p.getStatus()));
            if (canCancel) {
                // Restore stock
                itemRepo.findById(p.getItemId()).ifPresent(item -> {
                    item.setAvailableQty(Math.min(item.getTotalQty(), item.getAvailableQty() + p.getQuantity()));
                    itemRepo.save(item);
                });
                p.cancel();
                purchaseRepo.save(p);
            }
        });
        ra.addFlashAttribute("success", "Order #" + id + " cancelled.");
        return "redirect:/purchases";
    }
}
