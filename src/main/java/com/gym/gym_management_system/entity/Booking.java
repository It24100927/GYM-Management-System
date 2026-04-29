package com.gym.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Booking entity for class bookings
 *
 * @version 2.0
 * @date February 2026
 */
@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String className;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    @Column(nullable = false)
    private Integer maxCapacity = 1; // Max attendees for group classes

    @Column(nullable = false)
    private Integer currentCapacity = 0; // Current bookings count

    @Column(length = 500)
    private String memberNotes;

    @Column(length = 500)
    private String trainerNotes;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal fee;

    @Column(length = 50)
    private String paymentMethod; // CASH, CARD, ONLINE, BANK_TRANSFER

    @Column(length = 50)
    private String paymentStatus; // PENDING_ADMIN_APPROVAL, COMPLETED, FAILED, REFUNDED

    @Column(length = 255)
    private String slipFileName;

    @Column(length = 100)
    private String slipContentType;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] slipData;

    @Column(length = 100)
    private String portalTransactionRef;

    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (maxCapacity == null) {
            maxCapacity = 1;
        }
        if (currentCapacity == null) {
            currentCapacity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
