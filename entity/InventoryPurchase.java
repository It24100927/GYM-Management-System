package com.gym.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * InventoryPurchase entity
 * Represents a purchase made by a member from the gym store
 *
 * @version 1.0v
 * @date February 2026
 */
@Entity
@Table(name = "inventory_purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Price at time of purchase

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String paymentMethod = "CASH"; // CASH, CARD, WALLET, ONLINE

    @Column(nullable = false)
    private String paymentStatus = "PENDING"; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(nullable = false)
    private String fulfillmentStatus = "PENDING"; // PENDING, PROCESSING, READY, DELIVERED, CANCELLED

    @Column(length = 500)
    private String notes;

    private String transactionId; // External payment reference

    private String slipFileName;

    private String slipContentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] slipData;

    @Column(length = 120)
    private String portalTransactionRef;

    private LocalDateTime portalPaidAt;

    @Column(length = 500)
    private String adminNotes;

    private LocalDateTime reviewedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    private LocalDateTime deliveryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        purchaseDate = LocalDateTime.now();
        if (paymentMethod == null) paymentMethod = "CASH";
        if (paymentStatus == null) paymentStatus = "PENDING";
        if (fulfillmentStatus == null) fulfillmentStatus = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate total price based on quantity and unit price
     */
    public void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
