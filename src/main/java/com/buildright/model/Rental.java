package com.buildright.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long itemId;
    private int quantity;
    private Long operatorId = -1L;
    private Long agentId = -1L;

    private String startDate; // DD/MM/YYYY
    private String endDate;
    private int days;

    // PENDING -> AWAITING_APPROVAL -> APPROVED -> ACTIVE -> RETURNED | REJECTED | CANCELLED
    private String status = "PENDING";

    private boolean needsOperator;
    private boolean needsDelivery;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 1000)
    private String remarks = "";

    private Long paymentId = -1L;

    // Extension request
    private boolean extensionRequested = false;
    private String requestedEndDate = "";
    private String extensionStatus = "NONE"; // NONE|PENDING|APPROVED|REJECTED

    private boolean customerConfirmedDone = false;

    @Column(length = 3000)
    private String auditLog = ""; // pipe-separated entries

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Transient fields for display (not stored in DB)
    @Transient private String customerName;
    @Transient private String itemName;
    @Transient private String operatorName;
    @Transient private String agentName;
    @Transient private double totalAmount;

    public Rental() {}

    public Rental(Long customerId, Long itemId, int quantity,
                  String startDate, String endDate, int days,
                  boolean needsOperator, boolean needsDelivery,
                  String deliveryAddress) {
        this.customerId = customerId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.status = "PENDING";
        this.needsOperator = needsOperator;
        this.needsDelivery = needsDelivery;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        addAudit("Rental created. Status: PENDING");
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void addAudit(String msg) {
        String entry = "[" + LocalDateTime.now() + "] " + msg;
        if (auditLog == null || auditLog.isEmpty()) auditLog = entry;
        else auditLog = auditLog + "||" + entry;
        updatedAt = LocalDateTime.now();
    }

    public String[] getAuditEntries() {
        if (auditLog == null || auditLog.isEmpty()) return new String[0];
        return auditLog.split("\\|\\|");
    }

    public void setStatus(String s) {
        addAudit("Status changed: " + status + " -> " + s);
        status = s;
        updatedAt = LocalDateTime.now();
    }

    public void extendRental(String newEnd, int newDays) {
        addAudit("Extended: " + endDate + " -> " + newEnd + " (" + newDays + " days total)");
        endDate = newEnd;
        days = newDays;
        updatedAt = LocalDateTime.now();
    }

    public boolean isActiveRental() {
        return "ACTIVE".equals(status) || "APPROVED".equals(status) || "AWAITING_APPROVAL".equals(status);
    }

    public String getStatusBadgeClass() {
        return switch (status) {
            case "PENDING" -> "badge-warning";
            case "AWAITING_APPROVAL" -> "badge-info";
            case "APPROVED" -> "badge-primary";
            case "ACTIVE" -> "badge-success";
            case "RETURNED" -> "badge-secondary";
            case "CANCELLED" -> "badge-danger";
            case "REJECTED" -> "badge-danger";
            default -> "badge-secondary";
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public String getStatus() { return status; }
    public void setStatusDirectly(String status) { this.status = status; }

    public boolean isNeedsOperator() { return needsOperator; }
    public void setNeedsOperator(boolean needsOperator) { this.needsOperator = needsOperator; }

    public boolean isNeedsDelivery() { return needsDelivery; }
    public void setNeedsDelivery(boolean needsDelivery) { this.needsDelivery = needsDelivery; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public boolean isExtensionRequested() { return extensionRequested; }
    public void setExtensionRequested(boolean extensionRequested) { this.extensionRequested = extensionRequested; }

    public String getRequestedEndDate() { return requestedEndDate; }
    public void setRequestedEndDate(String requestedEndDate) { this.requestedEndDate = requestedEndDate; }

    public String getExtensionStatus() { return extensionStatus; }
    public void setExtensionStatus(String extensionStatus) { this.extensionStatus = extensionStatus; }

    public boolean isCustomerConfirmedDone() { return customerConfirmedDone; }
    public void setCustomerConfirmedDone(boolean customerConfirmedDone) { this.customerConfirmedDone = customerConfirmedDone; }

    public String getAuditLog() { return auditLog; }
    public void setAuditLog(String auditLog) { this.auditLog = auditLog; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
