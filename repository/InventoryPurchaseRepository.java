package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.InventoryPurchase;
import com.gym.gym_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryPurchase entity
 */
@Repository
public interface InventoryPurchaseRepository extends JpaRepository<InventoryPurchase, Long> {

    // Find purchases by user
    List<InventoryPurchase> findByUserOrderByPurchaseDateDesc(User user);

    List<InventoryPurchase> findByUserIdOrderByPurchaseDateDesc(Long userId);

    // Find by fulfillment status
    List<InventoryPurchase> findByFulfillmentStatusOrderByPurchaseDateDesc(String status);

    // Find by payment status
    List<InventoryPurchase> findByPaymentStatusOrderByPurchaseDateDesc(String status);

    // Find purchases within date range
    @Query("SELECT p FROM InventoryPurchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate ORDER BY p.purchaseDate DESC")
    List<InventoryPurchase> findPurchasesInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find today's purchases
    @Query("SELECT p FROM InventoryPurchase p WHERE CAST(p.purchaseDate AS date) = CURRENT_DATE ORDER BY p.purchaseDate DESC")
    List<InventoryPurchase> findTodaysPurchases();

    // Count purchases by status
    Long countByFulfillmentStatus(String status);

    Long countByPaymentStatus(String status);

    // Count actionable pending orders (fulfillment=PENDING, payment not FAILED/REFUNDED)
    @Query("SELECT COUNT(p) FROM InventoryPurchase p WHERE p.fulfillmentStatus = 'PENDING' " +
           "AND p.paymentStatus NOT IN ('FAILED', 'REFUNDED')")
    Long countActionablePendingOrders();

    // Count all orders (excluding failed/refunded payments)
    @Query("SELECT COUNT(p) FROM InventoryPurchase p WHERE p.paymentStatus NOT IN ('FAILED', 'REFUNDED')")
    Long countAllValidOrders();

    // Get total revenue
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM InventoryPurchase p WHERE p.paymentStatus = 'COMPLETED'")
    Double getTotalRevenue();

    // Get revenue within date range
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM InventoryPurchase p " +
           "WHERE p.paymentStatus = 'COMPLETED' AND p.purchaseDate BETWEEN :startDate AND :endDate")
    Double getRevenueInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get today's revenue - considers both purchaseDate (for direct purchases) and reviewedAt (for payment-gated approvals)
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM InventoryPurchase p " +
           "WHERE p.paymentStatus = 'COMPLETED' AND (CAST(p.purchaseDate AS date) = CURRENT_DATE OR CAST(p.reviewedAt AS date) = CURRENT_DATE)")
    Double getTodaysRevenue();

    // Get monthly revenue - considers both purchaseDate and reviewedAt
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM InventoryPurchase p " +
           "WHERE p.paymentStatus = 'COMPLETED' AND (" +
           "(YEAR(p.purchaseDate) = :year AND MONTH(p.purchaseDate) = :month) OR " +
           "(YEAR(p.reviewedAt) = :year AND MONTH(p.reviewedAt) = :month))")
    Double getMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // Count user purchases for an item (to enforce purchase limits)
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM InventoryPurchase p " +
           "WHERE p.user.id = :userId AND p.item.id = :itemId AND p.paymentStatus != 'FAILED' AND p.paymentStatus != 'REFUNDED'")
    Integer countUserPurchasesForItem(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // Find purchases by item
    List<InventoryPurchase> findByItemIdOrderByPurchaseDateDesc(Long itemId);

    // Count total purchases
    @Query("SELECT COUNT(p) FROM InventoryPurchase p WHERE p.paymentStatus = 'COMPLETED'")
    Long countCompletedPurchases();

    // Get most purchased items (top sellers)
    @Query("SELECT p.item.id, p.item.name, SUM(p.quantity) as totalSold " +
           "FROM InventoryPurchase p WHERE p.paymentStatus = 'COMPLETED' " +
           "GROUP BY p.item.id, p.item.name ORDER BY totalSold DESC")
    List<Object[]> getTopSellingItems();
}
