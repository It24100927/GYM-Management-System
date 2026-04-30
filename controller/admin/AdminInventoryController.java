package com.gym.gym_management_system.controller.admin;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.dto.InventoryItemDto;
import com.gym.gym_management_system.dto.PurchaseResponseDto;
import com.gym.gym_management_system.dto.StockAdjustmentDto;
import com.gym.gym_management_system.entity.InventoryItem;
import com.gym.gym_management_system.entity.InventoryPurchase;
import com.gym.gym_management_system.entity.InventoryStockLog;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.repository.UserRepository;
import com.gym.gym_management_system.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * Admin API Controller for Inventory Management
 * Handles CRUD operations for inventory items, stock management, and orders
 *
 * @version 1.0
 * @date February 2026
 */
@RestController
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    // ==================== ITEMS ====================

    /**
     * Get all inventory items
     */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAllItems() {
        log.info("GET /api/admin/inventory/items");
        List<InventoryItem> items = inventoryService.getAllItems();
        return ResponseEntity.ok(ApiResponse.success(items, "Items retrieved successfully"));
    }

    /**
     * Get item by ID
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getItemById(@PathVariable Long id) {
        log.info("GET /api/admin/inventory/items/{}", id);
        InventoryItem item = inventoryService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(item, "Item retrieved successfully"));
    }

    /**
     * Create new inventory item
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<InventoryItem>> createItem(@Valid @RequestBody InventoryItemDto dto) {
        log.info("POST /api/admin/inventory/items - Creating: {}", dto.getName());
        InventoryItem item = inventoryService.createItem(dto);
        return ResponseEntity.status(201).body(
                ApiResponse.created(item, "Item created successfully")
        );
    }

    /**
     * Update inventory item
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemDto dto) {
        log.info("PUT /api/admin/inventory/items/{}", id);
        InventoryItem item = inventoryService.updateItem(id, dto);
        return ResponseEntity.ok(ApiResponse.success(item, "Item updated successfully"));
    }

    /**
     * Delete inventory item
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        log.info("DELETE /api/admin/inventory/items/{}", id);
        inventoryService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully"));
    }

    /**
     * Get items by category
     */
    @GetMapping("/items/category/{category}")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getItemsByCategory(@PathVariable String category) {
        log.info("GET /api/admin/inventory/items/category/{}", category);
        List<InventoryItem> items = inventoryService.getItemsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(items, "Items retrieved successfully"));
    }

    /**
     * Search items
     */
    @GetMapping("/items/search")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> searchItems(@RequestParam String keyword) {
        log.info("GET /api/admin/inventory/items/search?keyword={}", keyword);
        List<InventoryItem> items = inventoryService.searchItems(keyword);
        return ResponseEntity.ok(ApiResponse.success(items, "Search completed"));
    }

    // ==================== STOCK MANAGEMENT ====================

    /**
     * Adjust stock for an item
     */
    @PostMapping("/stock/adjust")
    public ResponseEntity<ApiResponse<InventoryItem>> adjustStock(
            @Valid @RequestBody StockAdjustmentDto dto,
            Authentication authentication) {
        log.info("POST /api/admin/inventory/stock/adjust - Item: {}, Change: {}",
                dto.getItemId(), dto.getQuantityChange());

        User admin = getCurrentUser(authentication);
        InventoryItem item = inventoryService.adjustStock(dto, admin);
        return ResponseEntity.ok(ApiResponse.success(item, "Stock adjusted successfully"));
    }

    /**
     * Restock item (quick restock)
     */
    @PostMapping("/stock/restock/{itemId}")
    public ResponseEntity<ApiResponse<InventoryItem>> restockItem(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("POST /api/admin/inventory/stock/restock/{} - Qty: {}", itemId, quantity);

        User admin = getCurrentUser(authentication);
        InventoryItem item = inventoryService.restockItem(itemId, quantity, reason, admin);
        return ResponseEntity.ok(ApiResponse.success(item, "Item restocked successfully"));
    }

    /**
     * Get low stock items
     */
    @GetMapping("/stock/low")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStockItems() {
        log.info("GET /api/admin/inventory/stock/low");
        List<InventoryItem> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(items, "Low stock items retrieved"));
    }

    /**
     * Get out of stock items
     */
    @GetMapping("/stock/out")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getOutOfStockItems() {
        log.info("GET /api/admin/inventory/stock/out");
        List<InventoryItem> items = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.success(items, "Out of stock items retrieved"));
    }

    /**
     * Get stock logs for an item
     */
    @GetMapping("/stock/logs/{itemId}")
    public ResponseEntity<ApiResponse<List<InventoryStockLog>>> getStockLogs(@PathVariable Long itemId) {
        log.info("GET /api/admin/inventory/stock/logs/{}", itemId);
        List<InventoryStockLog> logs = inventoryService.getStockLogs(itemId);
        return ResponseEntity.ok(ApiResponse.success(logs, "Stock logs retrieved"));
    }

    /**
     * Get recent stock activity
     */
    @GetMapping("/stock/logs/recent")
    public ResponseEntity<ApiResponse<List<InventoryStockLog>>> getRecentStockLogs() {
        log.info("GET /api/admin/inventory/stock/logs/recent");
        List<InventoryStockLog> logs = inventoryService.getRecentStockLogs();
        return ResponseEntity.ok(ApiResponse.success(logs, "Recent stock logs retrieved"));
    }

    // ==================== ORDERS / PURCHASES ====================

    /**
     * Get all purchases/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getAllOrders() {
        log.info("GET /api/admin/inventory/orders");
        List<PurchaseResponseDto> orders = inventoryService.getAllPurchases();
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    /**
     * Get pending orders
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getPendingOrders() {
        log.info("GET /api/admin/inventory/orders/pending");
        List<PurchaseResponseDto> orders = inventoryService.getPendingPurchases();
        return ResponseEntity.ok(ApiResponse.success(orders, "Pending orders retrieved"));
    }

    /**
     * Update order fulfillment status
     */
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("PATCH /api/admin/inventory/orders/{}/status - {}", id, status);
        inventoryService.updateFulfillmentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Order status updated"));
    }

    @PostMapping("/orders/{id}/approve-payment")
    public ResponseEntity<ApiResponse<Void>> approveOrderPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication) {
        User admin = getCurrentUser(authentication);
        String effectiveNotes = notes;
        if ((effectiveNotes == null || effectiveNotes.isBlank()) && requestBody != null) {
            effectiveNotes = requestBody.get("notes");
        }
        inventoryService.approvePurchasePayment(id, admin, effectiveNotes);
        return ResponseEntity.ok(ApiResponse.success(null, "Order payment approved successfully"));
    }

    @PostMapping("/orders/{id}/reject-payment")
    public ResponseEntity<ApiResponse<Void>> rejectOrderPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication) {
        User admin = getCurrentUser(authentication);
        String effectiveNotes = notes;
        if ((effectiveNotes == null || effectiveNotes.isBlank()) && requestBody != null) {
            effectiveNotes = requestBody.get("notes");
        }
        inventoryService.rejectPurchasePayment(id, admin, effectiveNotes);
        return ResponseEntity.ok(ApiResponse.success(null, "Order payment rejected"));
    }

    @PostMapping("/orders/{id}/ship")
    public ResponseEntity<ApiResponse<Void>> shipOrder(
            @PathVariable Long id,
            Authentication authentication) {
        User admin = getCurrentUser(authentication);
        inventoryService.shipOrder(id, admin);
        return ResponseEntity.ok(ApiResponse.success(null, "Order marked as shipped"));
    }

    @GetMapping("/orders/{id}/slip")
    public ResponseEntity<byte[]> downloadOrderSlip(@PathVariable Long id) {
        InventoryPurchase order = inventoryService.getPurchaseSlip(id);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(order.getSlipContentType());
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(order.getSlipFileName(), StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(order.getSlipData());
    }

    /**
     * Cancel order
     */
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /api/admin/inventory/orders/{}/cancel", id);
        User admin = getCurrentUser(authentication);
        inventoryService.cancelPurchase(id, admin);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }

    // ==================== ANALYTICS ====================

    /**
     * Get inventory dashboard stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventoryStats() {
        log.info("GET /api/admin/inventory/stats");
        Map<String, Object> stats = inventoryService.getInventoryStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved successfully"));
    }

    /**
     * Get low stock alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLowStockAlerts() {
        log.info("GET /api/admin/inventory/alerts");
        List<Map<String, Object>> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts, "Alerts retrieved successfully"));
    }

    /**
     * Get top selling items
     */
    @GetMapping("/top-sellers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopSellers() {
        log.info("GET /api/admin/inventory/top-sellers");
        List<Map<String, Object>> topSellers = inventoryService.getTopSellingItems();
        return ResponseEntity.ok(ApiResponse.success(topSellers, "Top sellers retrieved"));
    }

    // ==================== HELPERS ====================

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.gym.gym_management_system.exception.ResourceNotFoundException("User", "email", email));
    }
}
