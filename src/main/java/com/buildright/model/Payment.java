package com.buildright.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rentalId;
    private double baseAmount;
    private double operatorFee;
    private double deliveryFee;
    private double totalAmount;

    private String status = "PENDING"; // PENDING | PAID | REFUNDED
    private String method = "";        // GCASH | BANK_TRANSFER
    private String paymentDate = "";
    private String referenceNo = "";

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient private String customerName;
    @Transient private String itemName;

    public Payment() {}

    public Payment(Long rentalId, double baseAmount, double operatorFee, double deliveryFee) {
        this.rentalId = rentalId;
        this.baseAmount = baseAmount;
        this.operatorFee = operatorFee;
        this.deliveryFee = deliveryFee;
        this.totalAmount = baseAmount + operatorFee + deliveryFee;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void markPaid(String method, String ref, String date) {
        this.method = method;
        this.referenceNo = ref;
        this.paymentDate = date;
        this.status = "PAID";
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        this.status = "REFUNDED";
        this.updatedAt = LocalDateTime.now();
    }

    public void addExtensionCharge(double addBase, double addOp) {
        baseAmount += addBase;
        operatorFee += addOp;
        totalAmount = baseAmount + operatorFee + deliveryFee;
        updatedAt = LocalDateTime.now();
    }

    public String getStatusBadgeClass() {
        return switch (status) {
            case "PENDING" -> "badge-warning";
            case "PAID" -> "badge-success";
            case "REFUNDED" -> "badge-info";
            default -> "badge-secondary";
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }

    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }

    public double getOperatorFee() { return operatorFee; }
    public void setOperatorFee(double operatorFee) { this.operatorFee = operatorFee; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
