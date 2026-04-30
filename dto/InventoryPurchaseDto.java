package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for purchase requests from users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPurchaseDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String paymentMethod; // CASH, CARD, WALLET, ONLINE

    private String notes;
}
