package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.ProfileUpdateDto;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.exception.BusinessException;
import com.gym.gym_management_system.exception.ResourceNotFoundException;
import com.gym.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user profile by ID
     */
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Get user profile by email
     */
    @Transactional(readOnly = true)
    public User getProfileByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateProfile(Long userId, ProfileUpdateDto dto) {
        log.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update basic info if provided
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        if (dto.getEmergencyContact() != null) {
            user.setEmergencyContact(dto.getEmergencyContact());
        }

        if (dto.getEmergencyPhone() != null) {
            user.setEmergencyPhone(dto.getEmergencyPhone());
        }

        // Check if email change is requested
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
            // Check if new email is already taken
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BusinessException("Email is already in use by another account");
            }
            user.setEmail(dto.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);

        return savedUser;
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("New password and confirmation do not match");
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Update profile by email (for authenticated users)
     */
    @Transactional
    public User updateProfileByEmail(String email, ProfileUpdateDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return updateProfile(user.getId(), dto);
    }

    /**
     * Change password by email (for authenticated users)
     */
    @Transactional
    public void changePasswordByEmail(String email, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        changePassword(user.getId(), currentPassword, newPassword, confirmPassword);
    }
}
