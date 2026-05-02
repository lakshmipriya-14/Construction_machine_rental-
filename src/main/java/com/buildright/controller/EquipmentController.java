package com.buildright.controller;

import com.buildright.config.CurrentUser;
import com.buildright.model.EquipmentItem;
import com.buildright.model.User;
import com.buildright.repository.EquipmentItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired private EquipmentItemRepository itemRepo;
    @Autowired private CurrentUser currentUser;

    // Upload directory — served via /uploads/** static mapping
    private static final String UPLOAD_DIR = "uploads/equipment/";

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf('.'));
            String filename = UUID.randomUUID().toString() + ext;
            Path dest = Paths.get(UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/equipment/" + filename;
        } catch (IOException e) {
            return null;
        }
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String category,
                       @RequestParam(required = false) String status) {
        List<EquipmentItem> items;
        if (category != null && !category.isBlank()) {
            items = itemRepo.findByCategory(category.toUpperCase());
        } else {
            items = itemRepo.findAll();
        }
        if ("active".equals(status)) items = items.stream().filter(EquipmentItem::isActive).toList();
        if ("inactive".equals(status)) items = items.stream().filter(i -> !i.isActive()).toList();

        model.addAttribute("items", items);
        model.addAttribute("currentUser", currentUser.get());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        return "equipment/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        User me = currentUser.get();
        if (!me.isStaff()) return "redirect:/equipment";
        model.addAttribute("item", new EquipmentItem());
        model.addAttribute("currentUser", me);
        return "equipment/form";
    }

    @PostMapping(value = "/new", consumes = "multipart/form-data")
    public String create(@RequestParam String name, @RequestParam String category,
                         @RequestParam String type, @RequestParam double dailyRate,
                         @RequestParam int totalQty, @RequestParam(defaultValue = "false") boolean requiresOperator,
                         @RequestParam(defaultValue = "false") boolean deliverable,
                         @RequestParam(defaultValue = "0") double deliveryFee,
                         @RequestParam String description,
                         @RequestParam(defaultValue = "false") boolean forSale,
                         @RequestParam(defaultValue = "0") double salePrice,
                         @RequestParam(required = false) MultipartFile imageFile,
                         RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/equipment";
        EquipmentItem item = new EquipmentItem(name, category, type, dailyRate, totalQty,
                requiresOperator, deliverable, deliveryFee, description);
        item.setForSale(forSale);
        item.setSalePrice(salePrice);
        String imgPath = saveImage(imageFile);
        if (imgPath != null) item.setImageUrl(imgPath);
        itemRepo.save(item);
        ra.addFlashAttribute("success", "Equipment '" + name + "' added successfully.");
        return "redirect:/equipment";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User me = currentUser.get();
        if (!me.isStaff()) return "redirect:/equipment";
        EquipmentItem item = itemRepo.findById(id).orElse(null);
        if (item == null) return "redirect:/equipment";
        model.addAttribute("item", item);
        model.addAttribute("currentUser", me);
        return "equipment/form";
    }

    @PostMapping(value = "/{id}/edit", consumes = "multipart/form-data")
    public String update(@PathVariable Long id,
                         @RequestParam String name, @RequestParam double dailyRate,
                         @RequestParam(defaultValue = "false") boolean requiresOperator,
                         @RequestParam(defaultValue = "false") boolean deliverable,
                         @RequestParam(defaultValue = "0") double deliveryFee,
                         @RequestParam String description,
                         @RequestParam(defaultValue = "false") boolean forSale,
                         @RequestParam(defaultValue = "0") double salePrice,
                         @RequestParam(defaultValue = "false") boolean active,
                         @RequestParam(required = false) MultipartFile imageFile,
                         RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/equipment";
        EquipmentItem item = itemRepo.findById(id).orElse(null);
        if (item == null) { ra.addFlashAttribute("error", "Item not found."); return "redirect:/equipment"; }

        item.setName(name);
        item.setDailyRate(dailyRate);
        item.setRequiresOperator(requiresOperator);
        item.setDeliverable(deliverable);
        item.setDeliveryFee(deliveryFee);
        item.setDescription(description);
        item.setForSale(forSale);
        item.setSalePrice(salePrice);
        item.setActive(active);
        // Only replace image if a new file was uploaded
        String imgPath = saveImage(imageFile);
        if (imgPath != null) item.setImageUrl(imgPath);
        itemRepo.save(item);
        ra.addFlashAttribute("success", "Equipment updated successfully.");
        return "redirect:/equipment";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/equipment";
        itemRepo.findById(id).ifPresent(item -> {
            item.setActive(!item.isActive());
            itemRepo.save(item);
        });
        ra.addFlashAttribute("success", "Equipment status toggled.");
        return "redirect:/equipment";
    }

    @PostMapping("/{id}/stock")
    public String updateStock(@PathVariable Long id,
                              @RequestParam int totalQty,
                              @RequestParam(required = false, defaultValue = "0") int availableQty,
                              RedirectAttributes ra) {
        if (!currentUser.isStaff()) return "redirect:/equipment";
        EquipmentItem item = itemRepo.findById(id).orElse(null);
        if (item == null) { ra.addFlashAttribute("error", "Item not found."); return "redirect:/equipment"; }
        if (totalQty < 0) { ra.addFlashAttribute("error", "Total quantity cannot be negative."); return "redirect:/equipment/" + id + "/edit"; }
        int rentedOut = item.getTotalQty() - item.getAvailableQty();
        item.setTotalQty(totalQty);
        // available = new total minus what is currently rented out (floor at 0)
        item.setAvailableQty(Math.max(0, totalQty - rentedOut));
        itemRepo.save(item);
        ra.addFlashAttribute("success", "Stock updated: total=" + totalQty + ", available=" + item.getAvailableQty());
        return "redirect:/equipment/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        EquipmentItem item = itemRepo.findById(id).orElse(null);
        if (item == null) return "redirect:/equipment";
        model.addAttribute("item", item);
        model.addAttribute("currentUser", currentUser.get());
        return "equipment/detail";
    }
}
