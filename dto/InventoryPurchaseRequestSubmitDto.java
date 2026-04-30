package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPurchaseRequestSubmitDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CASH|CARD|WALLET|ONLINE|BANK_TRANSFER)$",
            message = "Payment method must be CASH, CARD, WALLET, ONLINE, or BANK_TRANSFER")
    private String paymentMethod;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}

