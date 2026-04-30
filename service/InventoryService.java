package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.InventoryItemDto;
import com.gym.gym_management_system.dto.InventoryPurchaseDto;
import com.gym.gym_management_system.dto.InventoryPurchaseRequestSubmitDto;
import com.gym.gym_management_system.dto.OnlinePaymentPortalDto;
import com.gym.gym_management_system.dto.PurchaseResponseDto;
import com.gym.gym_management_system.dto.StockAdjustmentDto;
import com.gym.gym_management_system.entity.InventoryItem;
import com.gym.gym_management_system.entity.InventoryPurchase;
import com.gym.gym_management_system.entity.InventoryStockLog;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.exception.BusinessException;
import com.gym.gym_management_system.repository.InventoryItemRepository;
import com.gym.gym_management_system.repository.InventoryPurchaseRepository;
import com.gym.gym_management_system.repository.InventoryStockLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Inventory Management
 * Handles items, purchases, stock management, and analytics
 *
 * @version 1.0
 * @date February 2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final long MAX_SLIP_SIZE_BYTES = 5 * 1024 * 1024;
    private static final String METHOD_CARD = "CARD";
    private static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    private static final String METHOD_ONLINE = "ONLINE";
    private static final String PAYMENT_AWAITING_PORTAL = "AWAITING_PORTAL_PAYMENT";
    private static final String PAYMENT_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String PAYMENT_COMPLETED = "COMPLETED";
    private static final String PAYMENT_FAILED = "FAILED";
    private static final int DEFAULT_MAX_PURCHASE_LIMIT = 100;

    private final InventoryItemRepository itemRepository;
    private final InventoryPurchaseRepository purchaseRepository;
    private final InventoryStockLogRepository stockLogRepository;

    // ==================== ITEM MANAGEMENT ====================

    /**
     * Get all inventory items
     */
    public List<InventoryItem> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Get active items only (for user shop)
     */
    public List<InventoryItem> getActiveItems() {
        return itemRepository.findByStatus("ACTIVE");
    }

    /**
     * Get items by category
     */
    public List<InventoryItem> getItemsByCategory(String category) {
        return itemRepository.findByCategoryAndStatus(category, "ACTIVE");
    }

    /**
     * Get featured items
     */
    public List<InventoryItem> getFeaturedItems() {
        return itemRepository.findByIsFeaturedTrueAndStatus("ACTIVE");
    }

    /**
     * Search items
     */
    public List<InventoryItem> searchItems(String keyword) {
        return itemRepository.searchItems(keyword);
    }

    /**
     * Get item by ID
     */
    public InventoryItem getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Item not found with id: " + id));
    }

    /**
     * Create new inventory item
     */
    @Transactional
    public InventoryItem createItem(InventoryItemDto dto) {
        log.info("Creating new inventory item: {}", dto.getName());

        InventoryItem item = new InventoryItem();
        mapDtoToEntity(dto, item);

        InventoryItem saved = itemRepository.save(item);
        log.info("Created inventory item with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update inventory item
     */
    @Transactional
    public InventoryItem updateItem(Long id, InventoryItemDto dto) {
        log.info("Updating inventory item: {}", id);

        InventoryItem item = getItemById(id);
        mapDtoToEntity(dto, item);

        InventoryItem saved = itemRepository.save(item);
        log.info("Updated inventory item: {}", saved.getId());

        return saved;
    }

    /**
     * Delete inventory item
     */
    @Transactional
    public void deleteItem(Long id) {
        log.info("Deleting inventory item: {}", id);
        InventoryItem item = getItemById(id);
        itemRepository.delete(item);
    }

    /**
     * Map DTO to entity
     */
    private void mapDtoToEntity(InventoryItemDto dto, InventoryItem item) {
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setCategory(dto.getCategory());
        item.setPrice(dto.getPrice());
        item.setCostPrice(dto.getCostPrice() != null ? dto.getCostPrice() : dto.getPrice().multiply(BigDecimal.valueOf(0.6)));
        item.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        item.setLowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 10);
        item.setReorderQuantity(dto.getReorderQuantity() != null ? dto.getReorderQuantity() : 50);
        item.setSku(dto.getSku());
        item.setBrand(dto.getBrand());
        item.setImageUrl(dto.getImageUrl());
        item.setUnit(dto.getUnit() != null ? dto.getUnit() : "PCS");
        item.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        item.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        item.setMaxPurchasePerUser(dto.getMaxPurchasePerUser() != null ? dto.getMaxPurchasePerUser() : 100);
    }

    // ==================== STOCK MANAGEMENT ====================

    /**
     * Adjust stock quantity
     */
    @Transactional
    public InventoryItem adjustStock(StockAdjustmentDto dto, User performedBy) {
        log.info("Adjusting stock for item {}: {} units ({})", dto.getItemId(), dto.getQuantityChange(), dto.getChangeType());

        InventoryItem item = getItemById(dto.getItemId());
        int previousStock = item.getStockQuantity();
        int newStock = previousStock + dto.getQuantityChange();

        if (newStock < 0) {
            throw new BusinessException("Stock cannot be negative. Current stock: " + previousStock);
        }

        item.setStockQuantity(newStock);

        // Update status based on stock
        if (newStock <= 0) {
            item.setStatus("OUT_OF_STOCK");
        } else if ("OUT_OF_STOCK".equals(item.getStatus())) {
            item.setStatus("ACTIVE");
        }

        InventoryItem saved = itemRepository.save(item);

        // Create stock log
        InventoryStockLog stockLog = new InventoryStockLog();
        stockLog.setItem(item);
        stockLog.setPerformedBy(performedBy);
        stockLog.setChangeType(dto.getChangeType());
        stockLog.setQuantityChange(dto.getQuantityChange());
        stockLog.setPreviousStock(previousStock);
        stockLog.setNewStock(newStock);
        stockLog.setReason(dto.getReason());
        stockLogRepository.save(stockLog);

        log.info("Stock adjusted for item {}: {} -> {}", item.getId(), previousStock, newStock);

        return saved;
    }

    /**
     * Restock item
     */
    @Transactional
    public InventoryItem restockItem(Long itemId, int quantity, String reason, User performedBy) {
        StockAdjustmentDto dto = new StockAdjustmentDto();
        dto.setItemId(itemId);
        dto.setQuantityChange(quantity);
        dto.setChangeType("RESTOCK");
        dto.setReason(reason != null ? reason : "Regular restock");
        return adjustStock(dto, performedBy);
    }

    /**
     * Get low stock items
     */
    public List<InventoryItem> getLowStockItems() {
        return itemRepository.findLowStockItems();
    }

    /**
     * Get out of stock items
     */
    public List<InventoryItem> getOutOfStockItems() {
        return itemRepository.findOutOfStockItems();
    }

    /**
     * Get stock logs for an item
     */
    public List<InventoryStockLog> getStockLogs(Long itemId) {
        return stockLogRepository.findByItemIdOrderByCreatedAtDesc(itemId);
    }

    /**
     * Get recent stock logs
     */
    public List<InventoryStockLog> getRecentStockLogs() {
        return stockLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    // ==================== PURCHASE MANAGEMENT ====================

    /**
     * Create purchase (user buying from shop)
     */
    @Transactional
    public InventoryPurchase createPurchase(InventoryPurchaseDto dto, User user) {
        log.info("Creating purchase for user {}: item {}, qty {}", user.getId(), dto.getItemId(), dto.getQuantity());

        InventoryItem item = getItemById(dto.getItemId());

        // Validate item is available
        if (!"ACTIVE".equals(item.getStatus())) {
            throw new BusinessException("Item is not available for purchase");
        }

        // Validate stock
        if (item.getStockQuantity() < dto.getQuantity()) {
            throw new BusinessException("Insufficient stock. Available: " + item.getStockQuantity());
        }

        // Validate purchase limit
        Integer existingPurchases = purchaseRepository.countUserPurchasesForItem(user.getId(), item.getId());
        int maxAllowed = resolveMaxPurchaseLimit(item);
        if (existingPurchases + dto.getQuantity() > maxAllowed) {
            throw new BusinessException("Purchase limit exceeded. Max allowed: " + maxAllowed +
                    ", Already purchased: " + existingPurchases);
        }

        // Create purchase
        InventoryPurchase purchase = new InventoryPurchase();
        purchase.setUser(user);
        purchase.setItem(item);
        purchase.setQuantity(dto.getQuantity());
        purchase.setUnitPrice(item.getPrice());
        purchase.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        purchase.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH");
        purchase.setPaymentStatus("COMPLETED"); // Auto-complete for now
        purchase.setFulfillmentStatus("PENDING");
        purchase.setNotes(dto.getNotes());

        InventoryPurchase saved = purchaseRepository.save(purchase);

        // Deduct stock
        int previousStock = item.getStockQuantity();
        int newStock = previousStock - dto.getQuantity();
        item.setStockQuantity(newStock);

        if (newStock <= 0) {
            item.setStatus("OUT_OF_STOCK");
        }

        itemRepository.save(item);

        // Create stock log
        InventoryStockLog stockLog = new InventoryStockLog();
        stockLog.setItem(item);
        stockLog.setPerformedBy(user);
        stockLog.setChangeType("SALE");
        stockLog.setQuantityChange(-dto.getQuantity());
        stockLog.setPreviousStock(previousStock);
        stockLog.setNewStock(newStock);
        stockLog.setReason("Purchase by " + user.getFullName());
        stockLog.setReferenceId(saved.getId());
        stockLogRepository.save(stockLog);

        log.info("Purchase created successfully: ID {}, Total: ${}", saved.getId(), saved.getTotalPrice());

        return saved;
    }

    /**
     * Create purchase request that requires payment verification by admin.
     */
    @Transactional
    public InventoryPurchase createPurchaseRequest(InventoryPurchaseRequestSubmitDto dto, User user, MultipartFile slipFile) {
        log.info("Creating payment-gated purchase request for user {}: item {}, qty {}", user.getId(), dto.getItemId(), dto.getQuantity());

        InventoryItem item = getItemById(dto.getItemId());
        validatePurchaseRequest(item, dto.getQuantity(), user);

        String method = dto.getPaymentMethod().toUpperCase();
        InventoryPurchase purchase = new InventoryPurchase();
        purchase.setUser(user);
        purchase.setItem(item);
        purchase.setQuantity(dto.getQuantity());
        purchase.setUnitPrice(item.getPrice());
        purchase.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        purchase.setPaymentMethod(method);
        purchase.setFulfillmentStatus("PENDING");
        purchase.setNotes(dto.getNotes());

        if (METHOD_ONLINE.equals(method)) {
            if (slipFile != null && !slipFile.isEmpty()) {
                throw new BusinessException("Payment slip is not required for online portal payments");
            }
            purchase.setPaymentStatus(PAYMENT_AWAITING_PORTAL);
        } else if (methodRequiresSlip(method)) {
            validateSlipFile(slipFile);
            purchase.setPaymentStatus(PAYMENT_PENDING_APPROVAL);
            purchase.setSlipFileName(slipFile.getOriginalFilename());
            purchase.setSlipContentType(normalizeContentType(slipFile.getContentType()));
            try {
                purchase.setSlipData(slipFile.getBytes());
            } catch (IOException ex) {
                throw new BusinessException("Could not read uploaded payment slip");
            }
        } else {
            purchase.setPaymentStatus(PAYMENT_PENDING_APPROVAL);
        }

        return purchaseRepository.save(purchase);
    }

    /**
     * Capture online portal payment for a pending purchase request.
     */
    @Transactional
    public InventoryPurchase processOnlinePurchasePayment(Long purchaseId, Long userId, OnlinePaymentPortalDto dto) {
        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (!purchase.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only pay for your own purchase requests");
        }
        if (!METHOD_ONLINE.equals(purchase.getPaymentMethod())) {
            throw new BusinessException("This purchase is not configured for online payment portal");
        }
        if (!PAYMENT_AWAITING_PORTAL.equals(purchase.getPaymentStatus())) {
            throw new BusinessException("Online payment is already completed or not payable");
        }

        validatePortalCardData(dto);

        purchase.setPortalTransactionRef("SHOP-PORTAL-" + purchase.getId() + "-" + System.currentTimeMillis());
        purchase.setPortalPaidAt(LocalDateTime.now());
        purchase.setPaymentStatus(PAYMENT_PENDING_APPROVAL);

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public InventoryPurchase approvePurchasePayment(Long purchaseId, User admin, String adminNotes) {
        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (!PAYMENT_PENDING_APPROVAL.equals(purchase.getPaymentStatus())) {
            throw new BusinessException("Only purchase requests pending approval can be approved");
        }

        InventoryItem item = purchase.getItem();
        if (!"ACTIVE".equals(item.getStatus())) {
            throw new BusinessException("Item is not available for purchase");
        }
        if (item.getStockQuantity() < purchase.getQuantity()) {
            throw new BusinessException("Insufficient stock. Available: " + item.getStockQuantity());
        }

        int previousStock = item.getStockQuantity();
        int newStock = previousStock - purchase.getQuantity();
        item.setStockQuantity(newStock);
        if (newStock <= 0) {
            item.setStatus("OUT_OF_STOCK");
        }
        itemRepository.save(item);

        InventoryStockLog stockLog = new InventoryStockLog();
        stockLog.setItem(item);
        stockLog.setPerformedBy(admin);
        stockLog.setChangeType("SALE");
        stockLog.setQuantityChange(-purchase.getQuantity());
        stockLog.setPreviousStock(previousStock);
        stockLog.setNewStock(newStock);
        stockLog.setReason("Payment approved for order #" + purchase.getId());
        stockLog.setReferenceId(purchase.getId());
        stockLogRepository.save(stockLog);

        purchase.setPaymentStatus(PAYMENT_COMPLETED);
        purchase.setFulfillmentStatus("PROCESSING");
        purchase.setAdminNotes(adminNotes);
        purchase.setReviewedAt(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public InventoryPurchase rejectPurchasePayment(Long purchaseId, User admin, String adminNotes) {
        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (!PAYMENT_PENDING_APPROVAL.equals(purchase.getPaymentStatus())) {
            throw new BusinessException("Only purchase requests pending approval can be rejected");
        }

        purchase.setPaymentStatus(PAYMENT_FAILED);
        purchase.setFulfillmentStatus("CANCELLED");
        String note = (adminNotes != null && !adminNotes.isBlank()) ? adminNotes : "Payment rejected";
        purchase.setAdminNotes(note + " by " + admin.getFullName());
        purchase.setReviewedAt(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public InventoryPurchase shipOrder(Long purchaseId, User admin) {
        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (!PAYMENT_COMPLETED.equals(purchase.getPaymentStatus())) {
            throw new BusinessException("Only paid orders can be shipped");
        }
        if ("CANCELLED".equals(purchase.getFulfillmentStatus()) || "DELIVERED".equals(purchase.getFulfillmentStatus())) {
            throw new BusinessException("Order cannot be shipped in current state");
        }

        purchase.setFulfillmentStatus("READY");
        purchase.setAdminNotes("Order marked as shipped by " + admin.getFullName());
        purchase.setReviewedAt(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Transactional(readOnly = true)
    public InventoryPurchase getPurchaseSlip(Long purchaseId) {
        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (purchase.getSlipData() == null || purchase.getSlipData().length == 0) {
            throw new BusinessException("No uploaded payment slip is available for this order");
        }
        return purchase;
    }

    /**
     * Get user purchases
     */
    public List<PurchaseResponseDto> getUserPurchases(Long userId) {
        return purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId)
                .stream()
                .map(this::mapPurchaseToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all purchases (admin)
     */
    public List<PurchaseResponseDto> getAllPurchases() {
        return purchaseRepository.findAll()
                .stream()
                .map(this::mapPurchaseToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pending purchases
     */
    public List<PurchaseResponseDto> getPendingPurchases() {
        return purchaseRepository.findByFulfillmentStatusOrderByPurchaseDateDesc("PENDING")
                .stream()
                .map(this::mapPurchaseToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update purchase fulfillment status
     */
    @Transactional
    public InventoryPurchase updateFulfillmentStatus(Long purchaseId, String status) {
        log.info("Updating fulfillment status for purchase {}: {}", purchaseId, status);

        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if (!PAYMENT_COMPLETED.equals(purchase.getPaymentStatus()) && !"CANCELLED".equals(status)) {
            throw new BusinessException("Only paid orders can move through fulfillment statuses");
        }

        purchase.setFulfillmentStatus(status);

        if ("DELIVERED".equals(status)) {
            purchase.setDeliveryDate(LocalDateTime.now());
        }

        return purchaseRepository.save(purchase);
    }

    /**
     * Cancel purchase and restore stock
     */
    @Transactional
    public InventoryPurchase cancelPurchase(Long purchaseId, User performedBy) {
        log.info("Cancelling purchase: {}", purchaseId);

        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("Purchase not found"));

        if ("DELIVERED".equals(purchase.getFulfillmentStatus())) {
            throw new BusinessException("Cannot cancel a delivered purchase");
        }

        // Capture the original payment status BEFORE mutating it
        String originalPaymentStatus = purchase.getPaymentStatus();
        boolean wasCompleted = PAYMENT_COMPLETED.equals(originalPaymentStatus);

        InventoryItem item = purchase.getItem();
        int previousStock = item.getStockQuantity();
        int newStock = previousStock;

        // Restore stock only if this order already consumed stock.
        if (wasCompleted) {
            newStock = previousStock + purchase.getQuantity();
            item.setStockQuantity(newStock);

            if ("OUT_OF_STOCK".equals(item.getStatus()) && newStock > 0) {
                item.setStatus("ACTIVE");
            }

            itemRepository.save(item);
        }

        // Update purchase status
        purchase.setFulfillmentStatus("CANCELLED");
        purchase.setPaymentStatus(wasCompleted ? "REFUNDED" : PAYMENT_FAILED);

        // Create stock log using the captured original status
        InventoryStockLog stockLog = new InventoryStockLog();
        stockLog.setItem(item);
        stockLog.setPerformedBy(performedBy);
        stockLog.setChangeType(wasCompleted ? "RETURN" : "ADJUSTMENT");
        stockLog.setQuantityChange(wasCompleted ? purchase.getQuantity() : 0);
        stockLog.setPreviousStock(previousStock);
        stockLog.setNewStock(newStock);
        stockLog.setReason("Purchase cancelled - Order #" + purchaseId);
        stockLog.setReferenceId(purchase.getId());
        stockLogRepository.save(stockLog);

        return purchaseRepository.save(purchase);
    }

    /**
     * Map purchase to DTO
     */
    private PurchaseResponseDto mapPurchaseToDto(InventoryPurchase purchase) {
        PurchaseResponseDto dto = new PurchaseResponseDto();
        dto.setId(purchase.getId());
        dto.setUserId(purchase.getUser().getId());
        dto.setUserName(purchase.getUser().getFullName());
        dto.setUserEmail(purchase.getUser().getEmail());
        dto.setItemId(purchase.getItem().getId());
        dto.setItemName(purchase.getItem().getName());
        dto.setItemCategory(purchase.getItem().getCategory());
        dto.setQuantity(purchase.getQuantity());
        dto.setUnitPrice(purchase.getUnitPrice());
        dto.setTotalPrice(purchase.getTotalPrice());
        dto.setPaymentMethod(purchase.getPaymentMethod());
        dto.setPaymentStatus(purchase.getPaymentStatus());
        dto.setFulfillmentStatus(purchase.getFulfillmentStatus());
        dto.setNotes(purchase.getNotes());
        dto.setPortalTransactionRef(purchase.getPortalTransactionRef());
        dto.setPortalPaidAt(purchase.getPortalPaidAt());
        dto.setAdminNotes(purchase.getAdminNotes());
        dto.setPurchaseDate(purchase.getPurchaseDate());
        dto.setDeliveryDate(purchase.getDeliveryDate());
        return dto;
    }

    // ==================== ANALYTICS ====================

    /**
     * Get inventory dashboard stats
     */
    public Map<String, Object> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();

        // Item counts
        stats.put("totalItems", itemRepository.count());
        stats.put("activeItems", itemRepository.countByStatus("ACTIVE"));
        stats.put("lowStockItems", itemRepository.countLowStockItems());
        stats.put("outOfStockItems", itemRepository.countOutOfStockItems());

        // Stock value
        stats.put("totalStockValue", itemRepository.getTotalStockValue());

        // Purchase stats - use accurate counting methods
        stats.put("totalPurchases", purchaseRepository.countAllValidOrders());
        stats.put("pendingOrders", purchaseRepository.countActionablePendingOrders());
        stats.put("totalRevenue", purchaseRepository.getTotalRevenue());
        stats.put("todaysRevenue", purchaseRepository.getTodaysRevenue());

        // Current month revenue
        YearMonth currentMonth = YearMonth.now();
        stats.put("monthlyRevenue", purchaseRepository.getMonthlyRevenue(
                currentMonth.getYear(), currentMonth.getMonthValue()));

        return stats;
    }

    /**
     * Get low stock alerts
     */
    public List<Map<String, Object>> getLowStockAlerts() {
        return getLowStockItems().stream()
                .map(item -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("id", item.getId());
                    alert.put("name", item.getName());
                    alert.put("category", item.getCategory());
                    alert.put("currentStock", item.getStockQuantity());
                    alert.put("threshold", item.getLowStockThreshold());
                    alert.put("reorderQuantity", item.getReorderQuantity());
                    alert.put("severity", item.getStockQuantity() <= item.getLowStockThreshold() / 2 ? "HIGH" : "MEDIUM");
                    return alert;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get top selling items
     */
    public List<Map<String, Object>> getTopSellingItems() {
        return purchaseRepository.getTopSellingItems().stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("itemId", row[0]);
                    item.put("itemName", row[1]);
                    item.put("totalSold", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private void validatePurchaseRequest(InventoryItem item, Integer quantity, User user) {
        if (!"ACTIVE".equals(item.getStatus())) {
            throw new BusinessException("Item is not available for purchase");
        }
        if (item.getStockQuantity() < quantity) {
            throw new BusinessException("Insufficient stock. Available: " + item.getStockQuantity());
        }

        Integer existingPurchases = purchaseRepository.countUserPurchasesForItem(user.getId(), item.getId());
        int maxAllowed = resolveMaxPurchaseLimit(item);
        if (existingPurchases + quantity > maxAllowed) {
            throw new BusinessException("Purchase limit exceeded. Max allowed: " + maxAllowed +
                    ", Already purchased: " + existingPurchases);
        }
    }

    private void validateSlipFile(MultipartFile slipFile) {
        if (slipFile == null || slipFile.isEmpty()) {
            throw new BusinessException("Payment slip is required");
        }

        if (slipFile.getSize() > MAX_SLIP_SIZE_BYTES) {
            throw new BusinessException("Payment slip size cannot exceed 5 MB");
        }

        String contentType = normalizeContentType(slipFile.getContentType());
        boolean isAllowed = "application/pdf".equals(contentType)
                || "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);

        if (!isAllowed) {
            throw new BusinessException("Payment slip must be PDF, JPG, PNG, or WEBP");
        }
    }

    private void validatePortalCardData(OnlinePaymentPortalDto dto) {
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        if (dto.getExpiryYear() < currentYear
                || (dto.getExpiryYear() == currentYear && dto.getExpiryMonth() < currentMonth)) {
            throw new BusinessException("Card expiry date is in the past");
        }

        String cardDigits = dto.getCardNumber().replaceAll("\\s", "");
        if (cardDigits.length() < 13 || cardDigits.length() > 19 || !passesLuhn(cardDigits)) {
            throw new BusinessException("Card number is invalid");
        }
    }

    private boolean methodRequiresSlip(String method) {
        return METHOD_CARD.equals(method) || METHOD_BANK_TRANSFER.equals(method);
    }

    private boolean passesLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = cardNumber.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType.toLowerCase();
    }

    private int resolveMaxPurchaseLimit(InventoryItem item) {
        Integer configuredLimit = item.getMaxPurchasePerUser();
        if (configuredLimit == null || configuredLimit < 1) {
            return DEFAULT_MAX_PURCHASE_LIMIT;
        }
        return configuredLimit;
    }
}
