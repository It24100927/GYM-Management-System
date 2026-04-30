package com.gym.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * InventoryStockLog entity
 * Tracks all stock changes (purchases, restocks, adjustments, sales)
 *
 * @version 1.0
 * @date February 2026
 */
@Entity
@Table(name = "inventory_stock_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy; // Admin who made the change or User who purchased

    @Column(nullable = false)
    private String changeType; // RESTOCK, SALE, ADJUSTMENT, RETURN, DAMAGE, EXPIRED

    @Column(nullable = false)
    private Integer quantityChange; // Positive for additions, negative for deductions

    @Column(nullable = false)
    private Integer previousStock;

    @Column(nullable = false)
    private Integer newStock;

    @Column(length = 500)
    private String reason; // Description of why the change was made

    private Long referenceId; // ID of related purchase or order

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
