package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.InventoryStockLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryStockLog entity
 */
@Repository
public interface InventoryStockLogRepository extends JpaRepository<InventoryStockLog, Long> {

    // Find logs by item
    List<InventoryStockLog> findByItemIdOrderByCreatedAtDesc(Long itemId);

    // Find logs by change type
    List<InventoryStockLog> findByChangeTypeOrderByCreatedAtDesc(String changeType);

    // Find logs within date range
    @Query("SELECT l FROM InventoryStockLog l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<InventoryStockLog> findLogsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find recent logs
    List<InventoryStockLog> findTop50ByOrderByCreatedAtDesc();

    // Find logs by user who made the change
    List<InventoryStockLog> findByPerformedByIdOrderByCreatedAtDesc(Long userId);
}
