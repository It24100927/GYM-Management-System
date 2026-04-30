package com.gym.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * InventoryItem entity
 * Represents items in the gym store (supplements, merchandise, supplies)
 *
 * @version 1.0
 * @date February 2026
 */
@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category; // SUPPLEMENT, MERCHANDISE, EQUIPMENT, SUPPLIES

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice; // Cost price for profit tracking

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer lowStockThreshold = 10; // Alert when stock falls below this

    @Column(nullable = false)
    private Integer reorderQuantity = 50; // Suggested reorder quantity

    private String sku; // Stock Keeping Unit

    private String brand;

    private String imageUrl;

    private String unit = "PCS"; // PCS, KG, LTR, etc.

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, OUT_OF_STOCK

    private Boolean isFeatured = false;

    private Integer maxPurchasePerUser = 100; // Max quantity per purchase

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (stockQuantity == null) stockQuantity = 0;
        if (lowStockThreshold == null) lowStockThreshold = 10;
        if (reorderQuantity == null) reorderQuantity = 50;
        if (unit == null) unit = "PCS";
        if (isFeatured == null) isFeatured = false;
        if (maxPurchasePerUser == null) maxPurchasePerUser = 100;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Auto-update status based on stock (only for ACTIVE/OUT_OF_STOCK transitions)
        if (stockQuantity <= 0 && "ACTIVE".equals(status)) {
            status = "OUT_OF_STOCK";
        } else if ("OUT_OF_STOCK".equals(status) && stockQuantity > 0) {
            status = "ACTIVE";
        }
    }

    /**
     * Check if item is low on stock
     */
    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null
               && stockQuantity <= lowStockThreshold && stockQuantity > 0;
    }

    /**
     * Check if item is out of stock
     */
    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }
}
