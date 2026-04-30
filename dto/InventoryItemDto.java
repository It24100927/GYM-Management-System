package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating inventory items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {

    private Long id;

    @NotBlank(message = "Item name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category; // SUPPLEMENT, MERCHANDISE, EQUIPMENT, SUPPLIES

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Cost price must be non-negative")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockThreshold;

    @Min(value = 1, message = "Reorder quantity must be at least 1")
    private Integer reorderQuantity;

    private String sku;

    private String brand;

    private String imageUrl;

    private String unit;

    private String status;

    private Boolean isFeatured;

    @Min(value = 1, message = "Max purchase per user must be at least 1")
    private Integer maxPurchasePerUser;
}
