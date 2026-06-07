package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AdminPagesController
 * Routes: Manage Users, All Events, Reports, System Settings
 *
 * FRONTEND ONLY — No service or repository logic.
 */
@Controller
@RequestMapping("/admin")
public class AdminPagesController {

    @GetMapping("/users")
    public String manageUsers() {
        return "admin/manage-users";
    }

    @GetMapping("/events")
    public String allEvents() {
        return "admin/all-events";
    }

    @GetMapping("/reports")
    public String reports() {
        return "admin/reports";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/system-settings";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // Inject AdminService / UserService / EventService
    // ================================================
}
