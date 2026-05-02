package com.buildright.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_agents")
public class DeliveryAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long linkedUserId;
    private String name;
    private String vehicleType;
    private String licenseNo;
    private double dailyRate;
    private boolean available = true;
    private String workStatus = "IDLE"; // IDLE|ON_DELIVERY|DELIVERED|UNAVAILABLE
    private Long assignedRentalId = -1L;
    private double totalEarnings = 0.0;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DeliveryAgent() {}

    public DeliveryAgent(String name, String vehicleType, String licenseNo,
                         double dailyRate, Long linkedUserId) {
        this.name = name;
        this.vehicleType = vehicleType.toUpperCase();
        this.licenseNo = licenseNo.toUpperCase();
        this.dailyRate = dailyRate;
        this.linkedUserId = linkedUserId;
        this.available = true;
        this.workStatus = "IDLE";
        this.assignedRentalId = -1L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void assignDelivery(Long rentalId) {
        this.assignedRentalId = rentalId;
        this.workStatus = "ON_DELIVERY";
        this.available = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDelivered() {
        if ("ON_DELIVERY".equals(workStatus)) {
            workStatus = "DELIVERED";
            updatedAt = LocalDateTime.now();
        }
    }

    public void clearJob() {
        this.assignedRentalId = -1L;
        this.workStatus = "IDLE";
        this.available = true;
        this.updatedAt = LocalDateTime.now();
    }

    public String getWorkStatusBadgeClass() {
        return switch (workStatus) {
            case "IDLE" -> "badge-success";
            case "ON_DELIVERY" -> "badge-primary";
            case "DELIVERED" -> "badge-info";
            case "UNAVAILABLE" -> "badge-danger";
            default -> "badge-secondary";
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLinkedUserId() { return linkedUserId; }
    public void setLinkedUserId(Long linkedUserId) { this.linkedUserId = linkedUserId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getWorkStatus() { return workStatus; }
    public void setWorkStatus(String workStatus) { this.workStatus = workStatus; }

    public Long getAssignedRentalId() { return assignedRentalId; }
    public void setAssignedRentalId(Long assignedRentalId) { this.assignedRentalId = assignedRentalId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    public void addEarnings(double amount) { this.totalEarnings += amount; this.updatedAt = java.time.LocalDateTime.now(); }
}
