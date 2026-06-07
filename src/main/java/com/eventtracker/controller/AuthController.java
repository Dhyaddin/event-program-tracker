package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    // PLACEHOLDER — TEAMMATE IMPLEMENTATION AREA
    // POST /auth/login   → Spring Security will handle
    // POST /auth/register → UserService.register()
    // GET  /auth/logout  → Spring Security will handle
    // ================================================

}
