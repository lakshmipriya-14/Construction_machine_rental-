package com.buildright.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_items")
public class EquipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // MACHINE | TOOL

    @Column(nullable = false)
    private String type; // BULLDOZER, EXCAVATOR, BACKHOE, MIXER, COMPACTOR, etc.

    private double dailyRate;
    private int totalQty;
    private int availableQty;
    private boolean requiresOperator;
    private boolean deliverable;
    private double deliveryFee;

    @Column(length = 500)
    private String description;

    private boolean active = true;
    private boolean forSale = false;
    private double salePrice = 0;

    @Column(length = 500)
    private String imageUrl; // URL to equipment image

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public EquipmentItem() {}

    public EquipmentItem(String name, String category, String type,
                         double dailyRate, int totalQty,
                         boolean requiresOperator, boolean deliverable,
                         double deliveryFee, String description) {
        this.name = name;
        this.category = category.toUpperCase();
        this.type = type.toUpperCase();
        this.dailyRate = dailyRate;
        this.totalQty = totalQty;
        this.availableQty = totalQty;
        this.requiresOperator = requiresOperator;
        this.deliverable = deliverable;
        this.deliveryFee = deliveryFee;
        this.description = description;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public boolean isMachine() { return "MACHINE".equals(category); }
    public boolean isTool() { return "TOOL".equals(category); }
    public boolean isAvailable() { return availableQty > 0 && active; }
    public boolean canRent(int qty) { return qty >= 1 && qty <= availableQty && active; }
    public boolean isForSaleItem() { return forSale && isTool(); }

    public void rentOut(int qty) {
        if (qty > availableQty) throw new RuntimeException("Insufficient stock: only " + availableQty + " available.");
        availableQty -= qty;
        updatedAt = LocalDateTime.now();
    }

    public void returnUnits(int qty) {
        availableQty = Math.min(totalQty, availableQty + qty);
        updatedAt = LocalDateTime.now();
    }

    public String getStatusLabel() {
        if (!active) return "INACTIVE";
        if (availableQty == 0) return "FULLY RENTED";
        return "AVAILABLE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public int getTotalQty() { return totalQty; }
    public void setTotalQty(int totalQty) { this.totalQty = totalQty; }

    public int getAvailableQty() { return availableQty; }
    public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }

    public boolean isRequiresOperator() { return requiresOperator; }
    public void setRequiresOperator(boolean requiresOperator) { this.requiresOperator = requiresOperator; }

    public boolean isDeliverable() { return deliverable; }
    public void setDeliverable(boolean deliverable) { this.deliverable = deliverable; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isForSale() { return forSale; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
