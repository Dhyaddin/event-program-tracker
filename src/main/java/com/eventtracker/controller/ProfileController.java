package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.FeedbackService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.SavedEventService;
import com.eventtracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final RegistrationService registrationService;
    private final SavedEventService savedEventService;
    private final FeedbackService feedbackService;

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

    // ── Change password ──────────────────────────────────────────────────────
    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {

        if (newPassword == null || newPassword.length() < 6) {
            ra.addFlashAttribute("pwError", "New password must be at least 6 characters.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("pwError", "New password and confirmation do not match.");
            return "redirect:/profile";
        }

        boolean ok = userService.changePassword(userDetails.getUsername(), currentPassword, newPassword);
        if (ok) {
            ra.addFlashAttribute("pwSuccess", "Password changed successfully.");
        } else {
            ra.addFlashAttribute("pwError", "Your current password is incorrect.");
        }
        return "redirect:/profile";
    }

    // ── Delete account (self-service) ────────────────────────────────────────
    @PostMapping("/delete")
    public String deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes ra) {

        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/profile";
        }
        // Admins cannot delete their own account (protects the platform from losing its admin).
        if (user.getRole() == User.Role.ADMIN) {
            ra.addFlashAttribute("pwError", "Admin accounts cannot be deleted.");
            return "redirect:/profile";
        }

        // Remove the user's child rows first to avoid FK violations
        registrationService.deleteRegistrationsForUser(user);
        savedEventService.deleteSavedForUser(user);
        feedbackService.deleteFeedbackForUser(user);
        userService.deleteUser(user.getId());

        // End the session and clear auth so the now-deleted account is logged out
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/?accountDeleted=true";
    }
}
