package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profilePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute User updatedUser,
            RedirectAttributes redirectAttributes) {

        User existing = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update non-sensitive fields — never update email or password here
        existing.setName(updatedUser.getName());
        existing.setPhone(updatedUser.getPhone());

        userService.updateUser(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
}