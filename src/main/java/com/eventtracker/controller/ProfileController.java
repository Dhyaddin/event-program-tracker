package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    // PLACEHOLDER — TEAMMATE IMPLEMENTATION AREA
    //
    // @GetMapping
    // public String profile(Model model, Principal principal) {
    //     model.addAttribute("user", userService.findByEmail(principal.getName()));
    //     return "profile/profile";
    // }
    //
    // @PostMapping("/update")
    // public String updateProfile(@ModelAttribute UserDto dto) {
    //     userService.update(dto);
    //     return "redirect:/profile";
    // }
    //
    // @PostMapping("/change-password")
    // public String changePassword(@ModelAttribute PasswordDto dto) {
    //     userService.changePassword(dto);
    //     return "redirect:/profile";
    // }
    // ================================================

}
