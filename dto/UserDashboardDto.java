package com.gym.gym_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for User (Member) Dashboard
 * Contains all information displayed on member dashboard
 *
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardDto {

    // User info
    private Long userId;
    private String fullName;
    private String email;
    private String phone;

    // Membership information
    private MembershipInfoDto activeMembership;

    // Assigned trainer
    private TrainerInfoDto assignedTrainer;

    // Upcoming bookings
    private List<BookingResponseDto> upcomingBookings;

    // Active plans
    private WorkoutPlanSummaryDto activeWorkoutPlan;
    private MealPlanSummaryDto activeMealPlan;

    // Statistics
    private Long totalBookings;
    private Long completedSessions;
    private Double attendanceRate;
    private LocalDate memberSince;

    /**
     * Membership information summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipInfoDto {
        private Long membershipId;
        private String planName;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer daysRemaining;
        private Boolean isActive;
    }

    /**
     * Trainer information summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerInfoDto {
        private Long trainerId;
        private String fullName;
        private String email;
        private String phone;
        private String specialization;
        private Integer totalSessionsTogether;
        private LocalDate assignedDate;
    }

    /**
     * Workout plan summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutPlanSummaryDto {
        private Long planId;
        private String planName;
        private String difficulty;
        private String goal;
        private Integer durationWeeks;
        private Integer exerciseCount;
    }

    /**
     * Meal plan summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealPlanSummaryDto {
        private Long planId;
        private String planName;
        private String goal;
        private Integer dailyCalories;
        private Integer mealCount;
    }
}
