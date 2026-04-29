package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.*;
import com.gym.gym_management_system.entity.*;
import com.gym.gym_management_system.exception.BusinessException;
import com.gym.gym_management_system.exception.ResourceNotFoundException;
import com.gym.gym_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for User Dashboard
 * Aggregates all user-related data for dashboard display
 *
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDashboardService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BookingRepository bookingRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final MealPlanRepository mealPlanRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainerAssignmentRepository trainerAssignmentRepository;

    /**
     * Get complete dashboard data for a user
     */
    @Transactional(readOnly = true)
    public UserDashboardDto getUserDashboard(Long userId) {
        log.info("Fetching dashboard data for user ID: {}", userId);

        User user = getUserById(userId);
        LocalDate today = LocalDate.now();

        // Get active membership
        UserDashboardDto.MembershipInfoDto membership = getActiveMembership(user);

        // Get assigned trainer
        UserDashboardDto.TrainerInfoDto trainer = getAssignedTrainer(user);

        // Get upcoming bookings
        List<BookingResponseDto> upcomingBookings = getUpcomingBookings(user, today);

        // Get active workout plan
        UserDashboardDto.WorkoutPlanSummaryDto workoutPlan = getActiveWorkoutPlan(user);

        // Get active meal plan
        UserDashboardDto.MealPlanSummaryDto mealPlan = getActiveMealPlan(user);

        // Calculate statistics
        Long totalBookings = (long) bookingRepository.findByUserOrderByBookingDateDesc(user).size();
        Long completedSessions = bookingRepository.countCompletedBookingsByUser(user);
        Double attendanceRate = calculateAttendanceRate(totalBookings, completedSessions);

        return UserDashboardDto.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .activeMembership(membership)
                .assignedTrainer(trainer)
                .upcomingBookings(upcomingBookings)
                .activeWorkoutPlan(workoutPlan)
                .activeMealPlan(mealPlan)
                .totalBookings(totalBookings)
                .completedSessions(completedSessions)
                .attendanceRate(attendanceRate)
                .memberSince(user.getCreatedAt().toLocalDate())
                .build();
    }

    // Helper methods

    private User getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!"MEMBER".equals(user.getRole())) {
            throw new BusinessException("User is not a member");
        }

        return user;
    }

    private UserDashboardDto.MembershipInfoDto getActiveMembership(User user) {
        Optional<Membership> membershipOpt = membershipRepository.findActiveByUserId(user.getId());

        if (membershipOpt.isEmpty()) {
            return null;
        }

        Membership membership = membershipOpt.get();
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, membership.getEndDate());

        return UserDashboardDto.MembershipInfoDto.builder()
                .membershipId(membership.getId())
                .planName(membership.getPlanName())
                .status(membership.getStatus())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .daysRemaining((int) daysRemaining)
                .isActive("ACTIVE".equals(membership.getStatus()))
                .build();
    }

    private UserDashboardDto.TrainerInfoDto getAssignedTrainer(User user) {
        // First check TrainerAssignment table
        Optional<TrainerAssignment> assignment = trainerAssignmentRepository.findActiveAssignmentByMember(user);

        if (assignment.isPresent()) {
            User trainer = assignment.get().getTrainer();
            Integer totalSessions = bookingRepository.findByUserOrderByBookingDateDesc(user)
                    .stream()
                    .filter(b -> b.getTrainer() != null && b.getTrainer().getId().equals(trainer.getId()))
                    .toList()
                    .size();

            return UserDashboardDto.TrainerInfoDto.builder()
                    .trainerId(trainer.getId())
                    .fullName(trainer.getFullName())
                    .email(trainer.getEmail())
                    .phone(trainer.getPhone())
                    .specialization("Personal Training")
                    .totalSessionsTogether(totalSessions)
                    .assignedDate(assignment.get().getAssignedDate())
                    .build();
        }

        // Fallback: Find the most recent trainer from training sessions
        Optional<TrainingSession> recentSession = trainingSessionRepository
                .findByMemberOrderBySessionDateDesc(user)
                .stream()
                .findFirst();

        if (recentSession.isEmpty()) {
            return null;
        }

        User trainer = recentSession.get().getTrainer();
        Integer totalSessions = trainingSessionRepository
                .findByTrainerAndMemberOrderBySessionDateDesc(trainer, user)
                .size();

        return UserDashboardDto.TrainerInfoDto.builder()
                .trainerId(trainer.getId())
                .fullName(trainer.getFullName())
                .email(trainer.getEmail())
                .phone(trainer.getPhone())
                .specialization("Personal Training")
                .totalSessionsTogether(totalSessions)
                .build();
    }

    private List<BookingResponseDto> getUpcomingBookings(User user, LocalDate today) {
        return bookingRepository.findUpcomingBookingsForUser(user, today)
                .stream()
                .map(this::mapBookingToResponseDto)
                .collect(Collectors.toList());
    }

    private UserDashboardDto.WorkoutPlanSummaryDto getActiveWorkoutPlan(User user) {
        Optional<WorkoutPlan> planOpt = workoutPlanRepository.findByMemberAndIsActiveTrue(user);

        if (planOpt.isEmpty()) {
            return null;
        }

        WorkoutPlan plan = planOpt.get();

        return UserDashboardDto.WorkoutPlanSummaryDto.builder()
                .planId(plan.getId())
                .planName(plan.getPlanName())
                .difficulty(plan.getDifficulty())
                .goal(plan.getGoal())
                .durationWeeks(plan.getDurationWeeks())
                .exerciseCount(0) // Would need to count exercises
                .build();
    }

    private UserDashboardDto.MealPlanSummaryDto getActiveMealPlan(User user) {
        Optional<MealPlan> planOpt = mealPlanRepository.findByMemberAndIsActiveTrue(user);

        if (planOpt.isEmpty()) {
            return null;
        }

        MealPlan plan = planOpt.get();

        return UserDashboardDto.MealPlanSummaryDto.builder()
                .planId(plan.getId())
                .planName(plan.getPlanName())
                .goal(plan.getGoal())
                .dailyCalories(plan.getDailyCalories())
                .mealCount(0) // Would need to count meals
                .build();
    }

    private Double calculateAttendanceRate(Long total, Long completed) {
        if (total == 0) {
            return 0.0;
        }
        return (completed.doubleValue() / total.doubleValue()) * 100.0;
    }

    private BookingResponseDto mapBookingToResponseDto(Booking booking) {
        LocalDate today = LocalDate.now();
        boolean canCancel = booking.getBookingDate().isAfter(today) &&
                           ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()));

        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullName())
                .trainerId(booking.getTrainer() != null ? booking.getTrainer().getId() : null)
                .trainerName(booking.getTrainer() != null ? booking.getTrainer().getFullName() : null)
                .sessionDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .sessionType(booking.getClassName())
                .status(booking.getStatus())
                .currentCapacity(booking.getCurrentCapacity())
                .maxCapacity(booking.getMaxCapacity())
                .isFull(booking.getCurrentCapacity() >= booking.getMaxCapacity())
                .canCancel(canCancel)
                .memberNotes(booking.getMemberNotes())
                .trainerNotes(booking.getTrainerNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
