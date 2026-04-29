package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.dto.ProfileUpdateDto;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Profile Management
 * Handles profile updates for all user types
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get current user's profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(Authentication auth) {
        log.info("GET /api/profile - user: {}", auth.getName());

        User user = profileService.getProfileByEmail(auth.getName());
        Map<String, Object> profileData = mapUserToProfile(user);

        return ResponseEntity.ok(ApiResponse.success(profileData, "Profile retrieved successfully"));
    }

    /**
     * Update current user's profile
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @Valid @RequestBody ProfileUpdateDto dto,
            Authentication auth) {

        log.info("PUT /api/profile - user: {}", auth.getName());

        User updatedUser = profileService.updateProfileByEmail(auth.getName(), dto);
        Map<String, Object> profileData = mapUserToProfile(updatedUser);

        return ResponseEntity.ok(ApiResponse.success(profileData, "Profile updated successfully"));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ProfileUpdateDto dto,
            Authentication auth) {

        log.info("POST /api/profile/change-password - user: {}", auth.getName());

        if (dto.getCurrentPassword() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Current password, new password, and confirmation are required")
            );
        }

        profileService.changePasswordByEmail(
            auth.getName(),
            dto.getCurrentPassword(),
            dto.getNewPassword(),
            dto.getConfirmPassword()
        );

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Map User entity to profile response
     */
    private Map<String, Object> mapUserToProfile(User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        profile.put("status", user.getStatus());
        profile.put("address", user.getAddress());
        profile.put("emergencyContact", user.getEmergencyContact());
        profile.put("emergencyPhone", user.getEmergencyPhone());
        profile.put("dateOfBirth", user.getDateOfBirth());
        profile.put("gender", user.getGender());
        profile.put("createdAt", user.getCreatedAt());
        return profile;
    }
}
