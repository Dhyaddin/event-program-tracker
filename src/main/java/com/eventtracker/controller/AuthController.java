package com.eventtracker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AuthController
 * Routes: Login, Register
 *
 * FRONTEND ONLY — No authentication logic.
 * Authentication will be implemented by teammates via Spring Security.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    // ================================================
    // FRONTEND STUB — Prevents 405 Method Not Allowed
    // These redirects simulate success for UI testing.
    // Teammates will replace with real Spring Security.
    // ================================================

    @PostMapping("/login")
    public String loginPost() {
        // PLACEHOLDER — Spring Security will intercept this POST instead
        // For now, redirect to user dashboard to simulate login success
        return "redirect:/dashboard/user";
    }

    @PostMapping("/register")
    public String registerPost() {
        // PLACEHOLDER — UserService.register() will be implemented by teammate
        // For now, redirect to login page to simulate successful registration
        return "redirect:/auth/login";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // POST /auth/logout  → Spring Security will handle
    // ================================================

}
