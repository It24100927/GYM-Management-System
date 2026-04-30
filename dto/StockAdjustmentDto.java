package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock adjustments by admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange; // Can be positive or negative

    @NotBlank(message = "Change type is required")
    private String changeType; // RESTOCK, ADJUSTMENT, DAMAGE, EXPIRED, RETURN

    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;
}
