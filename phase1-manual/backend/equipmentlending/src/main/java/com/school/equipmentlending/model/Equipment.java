package com.school.equipmentlending.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // basic fields
    @Column(nullable = false)
    private String name;

    private String category;

    // e.g. "NEW", "GOOD", "FAIR", "POOR"
    @Column(name = "`condition`")
    private String condition;

    // number of units available in inventory
    @Column(nullable = false)
    private int quantity = 1;

    // quick flag: true when one or more units available (kept for convenience)
    @Column(nullable = false)
    private boolean available = true;

    // audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Equipment() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // ensure available is consistent with quantity
        if (this.quantity > 0) this.available = true;
        else this.available = false;
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
        // keep available consistent with quantity
        this.available = this.quantity > 0;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
