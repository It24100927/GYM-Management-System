package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.Booking;
import com.gym.gym_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for Booking entity with analytics queries
 *
 * @version 2.0
 * @date February 2026
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Count today's bookings (confirmed and completed)
     * Optimized query using COUNT, WHERE, and IN clause
     *
     * @param today Today's date
     * @return Count of today's bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :today AND b.status <> 'CANCELLED'")
    Long countTodayBookings(@Param("today") LocalDate today);

    /**
     * Count bookings in a date range (confirmed and completed only)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate AND b.status <> 'CANCELLED'")
    Long countBookingsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Count bookings by date
     *
     * @param date Booking date
     * @return Count of bookings on specified date
     */
    Long countByBookingDate(LocalDate date);

    /**
     * Count bookings by status
     *
     * @param status Booking status
     * @return Count of bookings with specified status
     */
    Long countByStatus(String status);

    /**
     * Find upcoming bookings for a user
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user " +
           "AND b.bookingDate >= :today AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY b.bookingDate ASC, b.startTime ASC")
    List<Booking> findUpcomingBookingsForUser(
        @Param("user") User user,
        @Param("today") LocalDate today
    );

    /**
     * Find all bookings for a user
     */
    List<Booking> findByUserOrderByBookingDateDesc(User user);

    /**
     * Check if user already has booking on specific date/time
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.user = :user " +
           "AND b.bookingDate = :date AND b.startTime = :startTime " +
           "AND b.status IN ('PENDING', 'CONFIRMED')")
    Boolean hasConflictingBooking(
        @Param("user") User user,
        @Param("date") LocalDate date,
        @Param("startTime") java.time.LocalTime startTime
    );

    /**
     * Find available sessions (not at full capacity)
     */
    @Query("SELECT b FROM Booking b WHERE b.bookingDate >= :today " +
           "AND b.currentCapacity < b.maxCapacity " +
           "AND b.status = 'CONFIRMED' " +
           "ORDER BY b.bookingDate ASC, b.startTime ASC")
    List<Booking> findAvailableSessions(@Param("today") LocalDate today);

    /**
     * Count completed bookings for a user
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user = :user AND b.status = 'COMPLETED'")
    Long countCompletedBookingsByUser(@Param("user") User user);

    /**
     * Find bookings by date range for a user
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user " +
           "AND b.bookingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY b.bookingDate ASC")
    List<Booking> findUserBookingsInDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    /**
     * Find bookings pending admin payment approval
     */
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :paymentStatus ORDER BY b.createdAt DESC")
    List<Booking> findByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    /**
     * Check whether a trainer already has an overlapping session for the same date.
     * Overlap rule: newStart < existingEnd AND newEnd > existingStart.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.trainer = :trainer " +
           "AND b.bookingDate = :date " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'SCHEDULED') " +
           "AND :startTime < b.endTime AND :endTime > b.startTime")
    Boolean hasTrainerTimeConflict(
        @Param("trainer") User trainer,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}
