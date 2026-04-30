package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryItem entity
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // Find by status
    List<InventoryItem> findByStatus(String status);

    // Find active items only
    List<InventoryItem> findByStatusNot(String status);

    // Find by category
    List<InventoryItem> findByCategory(String category);

    // Find active items by category
    List<InventoryItem> findByCategoryAndStatus(String category, String status);

    // Find featured items
    List<InventoryItem> findByIsFeaturedTrueAndStatus(String status);

    // Find low stock items
    @Query("SELECT i FROM InventoryItem i WHERE i.stockQuantity <= i.lowStockThreshold AND i.stockQuantity > 0")
    List<InventoryItem> findLowStockItems();

    // Find out of stock items
    @Query("SELECT i FROM InventoryItem i WHERE i.stockQuantity <= 0")
    List<InventoryItem> findOutOfStockItems();

    // Search by name or description
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND i.status = 'ACTIVE'")
    List<InventoryItem> searchItems(@Param("keyword") String keyword);

    // Find by SKU
    InventoryItem findBySku(String sku);

    // Count by status
    Long countByStatus(String status);

    // Count low stock items
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.stockQuantity <= i.lowStockThreshold AND i.stockQuantity > 0")
    Long countLowStockItems();

    // Count out of stock items
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.stockQuantity <= 0")
    Long countOutOfStockItems();

    // Get total stock value (all items with stock > 0)
    @Query("SELECT COALESCE(SUM(i.price * i.stockQuantity), 0) FROM InventoryItem i WHERE i.stockQuantity > 0")
    Double getTotalStockValue();

    // Get items ordered by sales (most popular)
    @Query("SELECT i FROM InventoryItem i WHERE i.status = 'ACTIVE' ORDER BY i.stockQuantity DESC")
    List<InventoryItem> findActiveItemsOrderedByStock();
}
