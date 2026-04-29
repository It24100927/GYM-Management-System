package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Count total members (role = MEMBER)
     * Optimized query using COUNT and WHERE clause
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'MEMBER'")
    Long countTotalMembers();

    /**
     * Count total trainers (role = TRAINER)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'TRAINER'")
    Long countTotalTrainers();

    /**
     * Count new members registered this month
     * Uses date comparison for filtering
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'MEMBER' AND u.createdAt >= :startOfMonth")
    Long countNewMembersThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * Count users by role (generic method)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") String role);

    /**
     * Find members by role with pagination
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * Search members by keyword (name, email, phone)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'MEMBER' AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchMembersByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
