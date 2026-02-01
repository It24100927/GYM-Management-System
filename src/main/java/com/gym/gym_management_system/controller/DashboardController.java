package com.gym.gym_management_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/admin")
    public String adminDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "dashboard/admin";
    }

    @GetMapping("/user")
    public String userDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "dashboard/user";
    }
}
