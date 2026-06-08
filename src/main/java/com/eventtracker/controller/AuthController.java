package com.eventtracker.controller;

import com.eventtracker.entity.User;
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

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "This email is already registered.");
            return "auth/register";
        }

        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Account created! Please log in.");
        return "redirect:/auth/login";
    }
}