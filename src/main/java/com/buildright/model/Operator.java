package com.buildright.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long linkedUserId;
    private String name;
    private String specialization;
    private double dailyRate;
    private boolean available = true;
    private String workStatus = "IDLE"; // IDLE|ON_DUTY|JOB_DONE|UNAVAILABLE
    private Long assignedRentalId = -1L;
    private String assignedRole = ""; // OPERATOR | DRIVER
    private double totalEarnings = 0.0;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Operator() {}

    public Operator(String name, String specialization, double dailyRate, Long linkedUserId) {
        this.name = name;
        this.specialization = specialization.toUpperCase();
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

    public void assignAsOperator(Long rentalId) {
        this.assignedRentalId = rentalId;
        this.assignedRole = "OPERATOR";
        this.workStatus = "ON_DUTY";
        this.available = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignAsDriver(Long rentalId) {
        this.assignedRentalId = rentalId;
        this.assignedRole = "DRIVER";
        this.workStatus = "ON_DUTY";
        this.available = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markJobDone() {
        if ("ON_DUTY".equals(workStatus)) {
            workStatus = "JOB_DONE";
            updatedAt = LocalDateTime.now();
        }
    }

    public void clearJob() {
        this.assignedRentalId = -1L;
        this.assignedRole = "";
        this.workStatus = "IDLE";
        this.available = true;
        this.updatedAt = LocalDateTime.now();
    }

    public String getWorkStatusBadgeClass() {
        return switch (workStatus) {
            case "IDLE" -> "badge-success";
            case "ON_DUTY" -> "badge-primary";
            case "JOB_DONE" -> "badge-info";
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

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getWorkStatus() { return workStatus; }
    public void setWorkStatus(String workStatus) { this.workStatus = workStatus; }

    public Long getAssignedRentalId() { return assignedRentalId; }
    public void setAssignedRentalId(Long assignedRentalId) { this.assignedRentalId = assignedRentalId; }

    public String getAssignedRole() { return assignedRole; }
    public void setAssignedRole(String assignedRole) { this.assignedRole = assignedRole; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    public void addEarnings(double amount) { this.totalEarnings += amount; this.updatedAt = java.time.LocalDateTime.now(); }
}
