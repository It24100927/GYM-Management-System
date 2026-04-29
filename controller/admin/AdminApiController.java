package com.gym.gym_management_system.controller.admin;

import com.gym.gym_management_system.dto.ApiResponse;
import com.gym.gym_management_system.entity.Booking;
import com.gym.gym_management_system.entity.MembershipPlan;
import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.repository.BookingRepository;
import com.gym.gym_management_system.repository.MembershipPlanRepository;
import com.gym.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin API Controller for managing trainers, plans, and bookings
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminApiController {

    private final UserRepository userRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== TRAINERS ====================

    @GetMapping("/trainers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTrainers() {
        log.info("GET /api/admin/trainers");

        List<Map<String, Object>> trainers = userRepository.findAll().stream()
                .filter(u -> "TRAINER".equals(u.getRole()))
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(trainers, "Trainers retrieved successfully"));
    }

    @PostMapping("/trainers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTrainer(@RequestBody Map<String, Object> request) {
        log.info("POST /api/admin/trainers");

        User trainer = new User();
        trainer.setFullName((String) request.get("fullName"));
        trainer.setEmail((String) request.get("email"));
        trainer.setPhone((String) request.get("phone"));
        trainer.setPassword(passwordEncoder.encode((String) request.get("password")));
        trainer.setRole("TRAINER");
        trainer.setStatus("ACTIVE");
        trainer.setSpecialization((String) request.get("specialization"));

        User saved = userRepository.save(trainer);

        return ResponseEntity.status(201).body(
            ApiResponse.created(mapUserToResponse(saved), "Trainer created successfully")
        );
    }

    @PutMapping("/trainers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTrainer(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        log.info("PUT /api/admin/trainers/{}", id);

        User trainer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (!"TRAINER".equals(trainer.getRole())) {
            throw new RuntimeException("User is not a trainer");
        }

        trainer.setFullName((String) request.get("fullName"));
        trainer.setEmail((String) request.get("email"));
        trainer.setPhone((String) request.get("phone"));
        if (request.containsKey("specialization")) {
            trainer.setSpecialization((String) request.get("specialization"));
        }
        
        // Update status if provided
        if (request.containsKey("status")) {
            trainer.setStatus((String) request.get("status"));
        }

        // Update password if provided
        String password = (String) request.get("password");
        if (password != null && !password.isEmpty()) {
            trainer.setPassword(passwordEncoder.encode(password));
        }

        User saved = userRepository.save(trainer);

        return ResponseEntity.ok(ApiResponse.success(mapUserToResponse(saved), "Trainer updated successfully"));
    }

    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrainer(@PathVariable Long id) {
        log.info("DELETE /api/admin/trainers/{}", id);

        User trainer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        
        userRepository.delete(trainer);

        return ResponseEntity.ok(ApiResponse.success("Trainer deleted successfully"));
    }

    // ==================== MEMBERSHIP PLANS ====================

    @GetMapping("/membership-plans")
    public ResponseEntity<ApiResponse<List<MembershipPlan>>> getAllPlans() {
        log.info("GET /api/admin/membership-plans");

        List<MembershipPlan> plans = membershipPlanRepository.findAll();

        return ResponseEntity.ok(ApiResponse.success(plans, "Plans retrieved successfully"));
    }

    @PostMapping("/membership-plans")
    public ResponseEntity<ApiResponse<MembershipPlan>> createPlan(@RequestBody MembershipPlan plan) {
        log.info("POST /api/admin/membership-plans");

        if (plan.getStatus() == null) plan.setStatus("ACTIVE");
        MembershipPlan saved = membershipPlanRepository.save(plan);

        return ResponseEntity.status(201).body(
            ApiResponse.created(saved, "Plan created successfully")
        );
    }

    @PutMapping("/membership-plans/{id}")
    public ResponseEntity<ApiResponse<MembershipPlan>> updatePlan(@PathVariable Long id, @RequestBody MembershipPlan plan) {
        log.info("PUT /api/admin/membership-plans/{}", id);

        MembershipPlan existing = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        existing.setName(plan.getName());
        existing.setDescription(plan.getDescription());
        existing.setDurationMonths(plan.getDurationMonths());
        existing.setPrice(plan.getPrice());
        existing.setFeatures(plan.getFeatures());
        existing.setCategory(plan.getCategory());
        existing.setIsPopular(plan.getIsPopular());
        existing.setStatus(plan.getStatus());

        MembershipPlan saved = membershipPlanRepository.save(existing);

        return ResponseEntity.ok(ApiResponse.success(saved, "Plan updated successfully"));
    }

    @DeleteMapping("/membership-plans/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long id) {
        log.info("DELETE /api/admin/membership-plans/{}", id);

        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        membershipPlanRepository.delete(plan);

        return ResponseEntity.ok(ApiResponse.success("Plan deleted successfully"));
    }

    // ==================== BOOKINGS ====================

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBookings() {
        log.info("GET /api/admin/bookings");

        List<Map<String, Object>> bookings = bookingRepository.findAll().stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("PATCH /api/admin/bookings/{}/status", id);
        
        Booking booking = bookingRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Booking not found"));
               
        booking.setStatus(status);
        Booking saved = bookingRepository.save(booking);
        
        return ResponseEntity.ok(ApiResponse.success(saved, "Booking status updated"));
    }
    
    // ==================== HELPERS ====================

    private Map<String, Object> mapUserToResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        map.put("status", user.getStatus());
        map.put("specialization", user.getSpecialization());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }

    private Map<String, Object> mapBookingToResponse(Booking booking) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", booking.getId());
        map.put("userName", booking.getUser() != null ? booking.getUser().getFullName() : "Unknown");
        map.put("trainerName", booking.getTrainer() != null ? booking.getTrainer().getFullName() : null);
        map.put("sessionType", booking.getClassName());
        map.put("bookingDate", booking.getBookingDate());
        map.put("sessionDate", booking.getBookingDate());
        map.put("startTime", booking.getStartTime());
        map.put("endTime", booking.getEndTime());
        map.put("status", booking.getStatus());
        return map;
    }
}
