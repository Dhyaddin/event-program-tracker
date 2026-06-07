package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ProfileController
 * Routes: User Profile Page
 *
 * FRONTEND ONLY — No service or repository logic.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping
    public String profile() {
        return "profile/profile";
    }

    // ================================================
    // FRONTEND STUBS — Prevents 405 Method Not Allowed
    // ================================================

    @PostMapping("/update")
    public String updateProfilePost() {
        // PLACEHOLDER — userService.update(dto) by teammate
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePasswordPost() {
        // PLACEHOLDER — userService.changePassword(dto) by teammate
        return "redirect:/profile";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // Inject UserService and replace the stubs above.
    // Add Principal/Authentication param for logged-in user.
    // ================================================

}
