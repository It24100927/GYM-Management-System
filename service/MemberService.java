package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.MemberDto;
import com.gym.gym_management_system.dto.MemberResponseDto;
import com.gym.gym_management_system.entity.Membership;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.exception.BusinessException;
import com.gym.gym_management_system.exception.DuplicateResourceException;
import com.gym.gym_management_system.exception.ResourceNotFoundException;
import com.gym.gym_management_system.repository.MembershipRepository;
import com.gym.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

/**
 * Service layer for Member Management
 * Handles all business logic for member CRUD operations
 *
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all members with pagination and search
     */
    @Transactional(readOnly = true)
    public Page<MemberResponseDto> getAllMembers(int page, int size, String search) {
        log.info("Fetching members - page: {}, size: {}, search: {}", page, size, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchMembersByKeyword(search, pageable);
        } else {
            users = userRepository.findByRole("MEMBER", pageable);
        }

        return users.map(this::mapToResponseDto);
    }

    /**
     * Get member by ID
     */
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberById(Long id) {
        log.info("Fetching member by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        if (!"MEMBER".equals(user.getRole())) {
            throw new BusinessException("User with ID " + id + " is not a member");
        }

        return mapToResponseDto(user);
    }

    /**
     * Create new member
     */
    @Transactional
    public MemberResponseDto createMember(MemberDto memberDto) {
        log.info("Creating new member: {}", memberDto.getEmail());

        // Validate email uniqueness
        if (userRepository.existsByEmail(memberDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + memberDto.getEmail());
        }

        // Validate required fields
        if (memberDto.getPassword() == null || memberDto.getPassword().trim().isEmpty()) {
            throw new BusinessException("Password is required for new members");
        }

        // Create user entity
        User user = new User();
        user.setFullName(memberDto.getFullName());
        user.setEmail(memberDto.getEmail());
        user.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        user.setRole("MEMBER");
        user.setPhone(memberDto.getPhone());
        user.setAddress(memberDto.getAddress());
        user.setDateOfBirth(memberDto.getDateOfBirth());
        user.setGender(memberDto.getGender());
        user.setEmergencyContact(memberDto.getEmergencyContact());
        user.setEmergencyPhone(memberDto.getEmergencyPhone());
        user.setMedicalConditions(memberDto.getMedicalConditions());
        user.setStatus(memberDto.getStatus());

        User savedUser = userRepository.save(user);
        log.info("Member created successfully with ID: {}", savedUser.getId());

        return mapToResponseDto(savedUser);
    }

    /**
     * Update existing member
     */
    @Transactional
    public MemberResponseDto updateMember(Long id, MemberDto memberDto) {
        log.info("Updating member ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        if (!"MEMBER".equals(user.getRole())) {
            throw new BusinessException("Cannot update: User is not a member");
        }

        // Check email uniqueness (excluding current user)
        if (!user.getEmail().equals(memberDto.getEmail()) &&
            userRepository.existsByEmail(memberDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + memberDto.getEmail());
        }

        // Update fields
        user.setFullName(memberDto.getFullName());
        user.setEmail(memberDto.getEmail());
        user.setPhone(memberDto.getPhone());
        user.setAddress(memberDto.getAddress());
        user.setDateOfBirth(memberDto.getDateOfBirth());
        user.setGender(memberDto.getGender());
        user.setEmergencyContact(memberDto.getEmergencyContact());
        user.setEmergencyPhone(memberDto.getEmergencyPhone());
        user.setMedicalConditions(memberDto.getMedicalConditions());
        user.setStatus(memberDto.getStatus());

        // Update password only if provided
        if (memberDto.getPassword() != null && !memberDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Member updated successfully: {}", id);

        return mapToResponseDto(updatedUser);
    }

    /**
     * Delete member (soft delete by setting status to INACTIVE)
     */
    @Transactional
    public void deleteMember(Long id) {
        log.info("Deleting member ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        if (!"MEMBER".equals(user.getRole())) {
            throw new BusinessException("Cannot delete: User is not a member");
        }

        // Soft delete - set status to INACTIVE
        user.setStatus("INACTIVE");
        userRepository.save(user);

        log.info("Member soft deleted successfully: {}", id);
    }

    /**
     * Update member status
     */
    @Transactional
    public MemberResponseDto updateMemberStatus(Long id, String status) {
        log.info("Updating member status - ID: {}, Status: {}", id, status);

        // Validate status
        if (!status.matches("^(ACTIVE|INACTIVE|SUSPENDED)$")) {
            throw new BusinessException("Invalid status. Must be ACTIVE, INACTIVE, or SUSPENDED");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        if (!"MEMBER".equals(user.getRole())) {
            throw new BusinessException("User is not a member");
        }

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("Member status updated successfully: {}", id);

        return mapToResponseDto(updatedUser);
    }

    /**
     * Map User entity to MemberResponseDto
     */
    private MemberResponseDto mapToResponseDto(User user) {
        // Get active membership if exists
        Optional<Membership> activeMembership = membershipRepository
                .findActiveByUserId(user.getId());

        return MemberResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .emergencyContact(user.getEmergencyContact())
                .emergencyPhone(user.getEmergencyPhone())
                .medicalConditions(user.getMedicalConditions())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .age(calculateAge(user.getDateOfBirth()))
                .hasActiveMembership(activeMembership.isPresent())
                .membershipPlan(activeMembership.map(Membership::getPlanName).orElse(null))
                .membershipExpiryDate(activeMembership.map(Membership::getEndDate).orElse(null))
                .build();
    }

    /**
     * Calculate age from date of birth
     */
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
