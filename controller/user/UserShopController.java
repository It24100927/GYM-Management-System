package com.gym.gym_management_system.controller.user;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.dto.InventoryPurchaseDto;
import com.gym.gym_management_system.dto.InventoryPurchaseRequestSubmitDto;
import com.gym.gym_management_system.dto.OnlinePaymentPortalDto;
import com.gym.gym_management_system.dto.PurchaseResponseDto;
import com.gym.gym_management_system.entity.InventoryItem;
import com.gym.gym_management_system.entity.InventoryPurchase;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.repository.UserRepository;
import com.gym.gym_management_system.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Shop Controller
 * Handles browsing and purchasing from the gym store
 *
 * @version 1.0
 * @date February 2026
 */
@RestController
@RequestMapping("/api/user/shop")
@PreAuthorize("hasRole('MEMBER')")
@RequiredArgsConstructor
@Slf4j
public class UserShopController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    // ==================== BROWSE PRODUCTS ====================

    /**
     * Get all available products
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAvailableProducts() {
        log.info("GET /api/user/shop/products");
        List<InventoryItem> products = inventoryService.getActiveItems();
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getProductById(@PathVariable Long id) {
        log.info("GET /api/user/shop/products/{}", id);
        InventoryItem product = inventoryService.getItemById(id);

        // Only return if active
        if (!"ACTIVE".equals(product.getStatus())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    /**
     * Get products by category
     */
    @GetMapping("/products/category/{category}")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/user/shop/products/category/{}", category);
        List<InventoryItem> products = inventoryService.getItemsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /**
     * Get featured products
     */
    @GetMapping("/products/featured")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getFeaturedProducts() {
        log.info("GET /api/user/shop/products/featured");
        List<InventoryItem> products = inventoryService.getFeaturedItems();
        return ResponseEntity.ok(ApiResponse.success(products, "Featured products retrieved"));
    }

    /**
     * Search products
     */
    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> searchProducts(@RequestParam String keyword) {
        log.info("GET /api/user/shop/products/search?keyword={}", keyword);
        List<InventoryItem> products = inventoryService.searchItems(keyword);
        return ResponseEntity.ok(ApiResponse.success(products, "Search completed"));
    }

    // ==================== PURCHASES ====================

    /**
     * Make a purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Void>> makePurchase(
            @Valid @RequestBody InventoryPurchaseDto dto,
            Authentication authentication) {
        log.info("POST /api/user/shop/purchase - Item: {}, Qty: {}", dto.getItemId(), dto.getQuantity());
        User user = getCurrentUser(authentication);
        inventoryService.createPurchase(dto, user);
        return ResponseEntity.status(201).body(
                ApiResponse.created(null, "Purchase successful! Your order is being processed.")
        );
    }

    /**
     * Submit purchase request with payment gate (slip upload or online portal flow)
     */
    @PostMapping(value = "/purchase/request", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitPurchaseRequest(
            @Valid @ModelAttribute InventoryPurchaseRequestSubmitDto dto,
            @RequestPart(value = "slip", required = false) MultipartFile slip,
            Authentication authentication) {
        log.info("POST /api/user/shop/purchase/request - Item: {}, Qty: {}", dto.getItemId(), dto.getQuantity());
        User user = getCurrentUser(authentication);
        InventoryPurchase purchase = inventoryService.createPurchaseRequest(dto, user, slip);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", purchase.getId());

        return ResponseEntity.status(201).body(
                ApiResponse.created(responseData, "Purchase request submitted successfully")
        );
    }

    /**
     * Complete online portal payment for a pending purchase request.
     */
    @PostMapping("/purchases/{id}/portal/pay")
    public ResponseEntity<ApiResponse<Void>> processOnlinePurchasePayment(
            @PathVariable Long id,
            @Valid @RequestBody OnlinePaymentPortalDto dto,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        inventoryService.processOnlinePurchasePayment(id, user.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success(null,
                "Online payment processed successfully and sent for admin approval"));
    }

    /**
     * Get user's purchase history
     */
    @GetMapping("/purchases")
    public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getMyPurchases(Authentication authentication) {
        log.info("GET /api/user/shop/purchases");

        User user = getCurrentUser(authentication);
        List<PurchaseResponseDto> purchases = inventoryService.getUserPurchases(user.getId());

        return ResponseEntity.ok(ApiResponse.success(purchases, "Purchases retrieved successfully"));
    }

    /**
     * Cancel a pending purchase
     */
    @PostMapping("/purchases/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPurchase(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /api/user/shop/purchases/{}/cancel", id);
        User user = getCurrentUser(authentication);
        // Verify ownership before cancelling
        List<PurchaseResponseDto> userPurchases = inventoryService.getUserPurchases(user.getId());
        boolean isOwner = userPurchases.stream().anyMatch(p -> p.getId().equals(id));
        if (!isOwner) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error(403, "You can only cancel your own purchases")
            );
        }
        inventoryService.cancelPurchase(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase cancelled successfully"));
    }

    // ==================== HELPERS ====================

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.gym.gym_management_system.exception.ResourceNotFoundException("User", "email", email));
    }
}
