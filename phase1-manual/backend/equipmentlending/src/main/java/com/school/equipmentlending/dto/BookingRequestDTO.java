package com.school.equipmentlending.dto;

import com.school.equipmentlending.model.BookingStatus;

import java.time.LocalDateTime;

public class BookingRequestDTO {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String requesterUsername;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int quantityRequested;
    private BookingStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public int getQuantityRequested() { return quantityRequested; }
    public void setQuantityRequested(int quantityRequested) { this.quantityRequested = quantityRequested; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
