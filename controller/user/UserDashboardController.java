package com.gym.gym_management_system.controller.user;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.dto.UserDashboardDto;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.repository.UserRepository;
import com.gym.gym_management_system.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User Dashboard
 *
 * @version 2.0
 */
@RestController
@RequestMapping("/api/user/dashboard")
@PreAuthorize("hasRole('MEMBER')")
@RequiredArgsConstructor
@Slf4j
public class UserDashboardController {

    private final UserDashboardService userDashboardService;
    private final UserRepository userRepository;

    /**
     * Get complete user dashboard data
     *
     * @param authentication Current authenticated user
     * @return Dashboard data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserDashboardDto>> getUserDashboard(
            Authentication authentication) {

        log.info("GET /api/user/dashboard - user: {}", authentication.getName());

        // Get actual user ID from authentication
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDashboardDto dashboard = userDashboardService.getUserDashboard(user.getId());

        return ResponseEntity.ok(
            ApiResponse.success(dashboard, "Dashboard data retrieved successfully")
        );
    }
}
