package com.gym.gym_management_system.controller.admin;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.dto.MemberDto;
import com.gym.gym_management_system.dto.MemberResponseDto;
import com.gym.gym_management_system.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Member Management (Admin)
 * Provides endpoints for CRUD operations on members
 *
 * @version 2.0
 */
@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberController {

    private final MemberService memberService;

    /**
     * Get all members with pagination and search
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param search Search keyword
     * @return Paginated list of members
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponseDto>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        log.info("GET /api/admin/members - page: {}, size: {}, search: {}", page, size, search);

        Page<MemberResponseDto> members = memberService.getAllMembers(page, size, search);
        return ResponseEntity.ok(
            ApiResponse.success(members, "Members retrieved successfully")
        );
    }

    /**
     * Get member by ID
     *
     * @param id Member ID
     * @return Member details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMemberById(@PathVariable Long id) {
        log.info("GET /api/admin/members/{}", id);

        MemberResponseDto member = memberService.getMemberById(id);
        return ResponseEntity.ok(
            ApiResponse.success(member, "Member found")
        );
    }

    /**
     * Create new member
     *
     * @param memberDto Member data
     * @return Created member
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponseDto>> createMember(
            @Valid @RequestBody MemberDto memberDto) {

        log.info("POST /api/admin/members - email: {}", memberDto.getEmail());

        MemberResponseDto created = memberService.createMember(memberDto);
        return ResponseEntity.status(201).body(
            ApiResponse.created(created, "Member created successfully")
        );
    }

    /**
     * Update existing member
     *
     * @param id Member ID
     * @param memberDto Updated member data
     * @return Updated member
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberDto memberDto) {

        log.info("PUT /api/admin/members/{}", id);

        MemberResponseDto updated = memberService.updateMember(id, memberDto);
        return ResponseEntity.ok(
            ApiResponse.success(updated, "Member updated successfully")
        );
    }

    /**
     * Delete member (soft delete)
     *
     * @param id Member ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        log.info("DELETE /api/admin/members/{}", id);

        memberService.deleteMember(id);
        return ResponseEntity.ok(
            ApiResponse.success("Member deleted successfully")
        );
    }

    /**
     * Update member status
     *
     * @param id Member ID
     * @param status New status (ACTIVE, INACTIVE, SUSPENDED)
     * @return Updated member
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateMemberStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        log.info("PATCH /api/admin/members/{}/status - status: {}", id, status);

        MemberResponseDto updated = memberService.updateMemberStatus(id, status);
        return ResponseEntity.ok(
            ApiResponse.success(updated, "Member status updated successfully")
        );
    }
}
