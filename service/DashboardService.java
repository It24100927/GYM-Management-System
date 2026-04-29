package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.DashboardStatsDto;
import com.gym.gym_management_system.repository.BookingRepository;
import com.gym.gym_management_system.repository.MembershipRepository;
import com.gym.gym_management_system.repository.PaymentRepository;
import com.gym.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Service for dashboard analytics and statistics
 *
 * Provides optimized methods to calculate various metrics
 * for admin dashboard using native queries and aggregations
 *
 * @version 2.0
 * @date February 2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Get comprehensive dashboard statistics
     *
     * This method performs multiple optimized queries to gather
     * all required statistics for the admin dashboard
     *
     * @return DashboardStatsDto with all statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsDto getAdminDashboardStats() {
        log.info("Fetching admin dashboard statistics");

        // Get current date and month boundaries
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        // Execute optimized queries in parallel-ready format
        Long totalMembers = getTotalMembers();
        Long totalTrainers = getTotalTrainers();
        Long activeMemberships = getActiveMemberships();
        Long todayBookings = getTodayBookings(today);
        BigDecimal monthlyRevenue = getMonthlyRevenue(startOfMonth);
        Long newMembersThisMonth = getNewMembersThisMonth(startOfMonth);

        // Build and return DTO
        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalMembers(totalMembers)
                .totalTrainers(totalTrainers)
                .activeMemberships(activeMemberships)
                .todayBookings(todayBookings)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .newMembersThisMonth(newMembersThisMonth)
                .build();

        log.info("Dashboard stats calculated: Members={}, Trainers={}, Active Memberships={}, Today Bookings={}, Monthly Revenue={}",
                totalMembers, totalTrainers, activeMemberships, todayBookings, monthlyRevenue);

        return stats;
    }

    /**
     * Get total number of members
     * Uses optimized COUNT query
     */
    private Long getTotalMembers() {
        try {
            return userRepository.countTotalMembers();
        } catch (Exception e) {
            log.error("Error counting total members", e);
            return 0L;
        }
    }

    /**
     * Get total number of trainers
     * Uses optimized COUNT query
     */
    private Long getTotalTrainers() {
        try {
            return userRepository.countTotalTrainers();
        } catch (Exception e) {
            log.error("Error counting total trainers", e);
            return 0L;
        }
    }

    /**
     * Get number of active memberships
     * Uses optimized COUNT with WHERE clause
     */
    private Long getActiveMemberships() {
        try {
            return membershipRepository.countActiveMemberships();
        } catch (Exception e) {
            log.error("Error counting active memberships", e);
            return 0L;
        }
    }

    /**
     * Get today's bookings count
     * Uses optimized COUNT with date filter
     */
    private Long getTodayBookings(LocalDate today) {
        try {
            return bookingRepository.countTodayBookings(today);
        } catch (Exception e) {
            log.error("Error counting today's bookings", e);
            return 0L;
        }
    }

    /**
     * Calculate monthly revenue
     * Uses optimized SUM query with date filter
     */
    private BigDecimal getMonthlyRevenue(LocalDateTime startOfMonth) {
        try {
            BigDecimal revenue = paymentRepository.calculateMonthlyRevenue(startOfMonth);
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating monthly revenue", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get count of new members this month
     * Uses optimized COUNT with date filter
     */
    private Long getNewMembersThisMonth(LocalDateTime startOfMonth) {
        try {
            return userRepository.countNewMembersThisMonth(startOfMonth);
        } catch (Exception e) {
            log.error("Error counting new members this month", e);
            return 0L;
        }
    }

    /**
     * Get statistics for a specific date range
     * Useful for custom reports
     */
    @Transactional(readOnly = true)
    public DashboardStatsDto getStatsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching stats for date range: {} to {}", startDate, endDate);

        BigDecimal revenue = paymentRepository.calculateRevenueBetweenDates(startDate, endDate);

        return DashboardStatsDto.builder()
                .monthlyRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .build();
    }
}
