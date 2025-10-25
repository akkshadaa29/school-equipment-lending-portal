package com.school.equipmentlending.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    // e.g. "NEW", "GOOD", "FAIR", "POOR"
    @Column(name = "`condition`")
    private String condition;

    // total number of units in inventory (authoritative)
    @Column(nullable = false)
    private int quantity = 1;

    // convenience flag (kept for quick reads; we also compute availability from loans)
    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Equipment() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // ensure available is consistent with quantity at creation
        this.available = this.quantity > 0;
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
        this.available = this.quantity > 0;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
