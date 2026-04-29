package com.gym.gym_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for booking information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private Long trainerId;
    private String trainerName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionType;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private Integer currentCapacity;
    private Integer maxCapacity;
    private Boolean isFull;
    private Boolean canCancel;
    private String memberNotes;
    private String trainerNotes;
    private java.math.BigDecimal fee;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
