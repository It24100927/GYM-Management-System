package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.DashboardStatsDto;
import com.gym.gym_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for Dashboard Analytics
 *
 * @version 2.0
 * @date February 2026
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardApiController {

    private final DashboardService dashboardService;

    /**
     * Get admin dashboard statistics
     *
     * @return ResponseEntity with DashboardStatsDto
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDto> getAdminStats() {
        try {
            log.info("API request: GET /api/dashboard/admin/stats");
            DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching admin stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
