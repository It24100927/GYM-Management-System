package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating/updating bookings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    @NotNull(message = "Session date is required")
    @FutureOrPresent(message = "Session date must be today or in the future")
    private LocalDate sessionDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Session type is required")
    @Pattern(regexp = "^(PERSONAL_TRAINING|GROUP_CLASS|CONSULTATION)$",
             message = "Invalid session type")
    private String sessionType;

    private Long trainerId; // Optional - can be assigned later

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String memberNotes;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CARD|CASH|ONLINE)$", message = "Invalid payment method")
    private String paymentMethod;
}
