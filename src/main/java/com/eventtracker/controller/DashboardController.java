package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * DashboardController
 * Routes: User Dashboard, Organiser Dashboard, Admin Dashboard
 *
 * FRONTEND ONLY — No service or repository logic.
 * Mock/dummy data is rendered directly in Thymeleaf templates.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/user")
    public String userDashboard() {
        return "dashboard/dashboard-user";
    }

    @GetMapping("/organiser")
    public String organiserDashboard() {
        return "dashboard/dashboard-organiser";
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "dashboard/dashboard-admin";
    }

    // ================================================
    // PLACEHOLDER — TEAMMATE IMPLEMENTATION AREA
    // Add Model parameter + service calls here later:
    //
    // @GetMapping("/user")
    // public String userDashboard(Model model) {
    //     model.addAttribute("events", eventService.getRegisteredEvents(userId));
    //     model.addAttribute("stats", userService.getStats(userId));
    //     return "dashboard/dashboard-user";
    // }
    // ================================================

}
