package com.school.equipmentlending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for requesting a direct (immediate) loan/borrow of equipment.
 * Used in POST /api/loans/borrow
 */
public class BorrowRequest {

    @NotNull(message = "equipmentId is required")
    private Long equipmentId;

    @Min(value = 1, message = "quantity must be >= 1")
    private Integer quantity = 1; // optional, defaults to 1

    @Min(value = 1, message = "days must be >= 1")
    private Integer days = 7; // optional, defaults to 7 days

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
