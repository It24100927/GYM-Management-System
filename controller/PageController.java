package com.gym.gym_management_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page Controller for all navigation routes
 * Handles page rendering for Admin, Trainer, and User dashboards
 */
@Controller
public class PageController {

    // ===================== ADMIN ROUTES =====================

    @GetMapping("/admin/members")
    public String adminMembers(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Member Management");
        return "admin/members";
    }

    @GetMapping("/admin/trainers")
    public String adminTrainers(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Trainer Management");
        return "admin/trainers";
    }

    @GetMapping("/admin/plans")
    public String adminPlans(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Membership Plans");
        return "admin/plans";
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Booking Management");
        return "admin/bookings";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Reports");
        return "admin/reports";
    }

    // ===================== TRAINER ROUTES =====================

    @GetMapping("/trainer/sessions")
    public String trainerSessions(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Sessions");
        return "trainer/sessions";
    }

    @GetMapping("/trainer/sessions/new")
    public String trainerNewSession(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Schedule Session");
        return "trainer/session-form";
    }

    @GetMapping("/trainer/clients")
    public String trainerClients(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Clients");
        return "trainer/clients";
    }

    @GetMapping("/trainer/workout-plans")
    public String trainerWorkoutPlans(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Workout Plans");
        return "trainer/workout-plans";
    }

    @GetMapping("/trainer/meal-plans")
    public String trainerMealPlans(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Meal Plans");
        return "trainer/meal-plans";
    }

    @GetMapping("/trainer/profile")
    public String trainerProfile(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Profile");
        return "trainer/profile";
    }

    // ===================== USER/MEMBER ROUTES =====================

    @GetMapping("/user/membership-plans")
    public String userMembershipPlans(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Membership Plans");
        return "user/membership-plans";
    }

    @GetMapping("/user/bookings")
    public String userBookings(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "Book a Session");
        return "user/bookings";
    }

    @GetMapping("/user/my-bookings")
    public String userMyBookings(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Bookings");
        return "user/my-bookings";
    }

    @GetMapping("/user/workout-plan")
    public String userWorkoutPlan(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Workout Plan");
        return "user/workout-plan";
    }

    @GetMapping("/user/meal-plan")
    public String userMealPlan(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Meal Plan");
        return "user/meal-plan";
    }

    @GetMapping("/user/profile")
    public String userProfile(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("pageTitle", "My Profile");
        return "user/profile";
    }
}
