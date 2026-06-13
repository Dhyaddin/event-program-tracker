package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.ActivityLogService;
import com.eventtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ActivityLogService activityLogService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());   // Empty user object for the form
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        // Password policy: at least 6 characters AND must contain both letters and numbers.
        String pw = user.getPassword();
        if (pw == null || pw.length() < 6 || !pw.matches(".*[A-Za-z].*") || !pw.matches(".*\\d.*")) {
            model.addAttribute("passwordError",
                    "Password must be at least 6 characters and include both letters and numbers.");
            return "auth/register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "This email is already registered.");
            return "auth/register";
        }

        userService.registerUser(user);
        activityLogService.log("USER",
                "New " + (user.getRole() != null ? user.getRole().name().toLowerCase() : "user")
                        + " account registered: " + user.getName(),
                user.getName());
        redirectAttributes.addFlashAttribute("successMessage",
                "Account created! Please log in.");
        return "redirect:/auth/login";
    }
}