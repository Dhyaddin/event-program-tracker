package com.eventtracker.controller;

import com.eventtracker.entity.Registration;
import com.eventtracker.entity.SavedEvent;
import com.eventtracker.entity.User;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.SavedEventService;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * UserPagesController
 * Routes: My Registrations, Saved Events
 *
 * Both are wired to the database, scoped to the logged-in user.
 */
@Controller
@RequiredArgsConstructor
public class UserPagesController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final SavedEventService savedEventService;

    @GetMapping("/my-registrations")
    public String myRegistrations(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Registration> registrations = registrationService.findByUser(user);
        model.addAttribute("registrations", registrations);
        model.addAttribute("totalRegistered", registrations.size());
        model.addAttribute("upcomingCount",
                registrations.stream()
                        .filter(r -> r.getStatus() == Registration.Status.REGISTERED)
                        .count());
        model.addAttribute("attendedCount",
                registrations.stream()
                        .filter(r -> r.getStatus() == Registration.Status.ATTENDED)
                        .count());
        return "user/my-registrations";
    }

    @GetMapping("/my-saved")
    public String savedEvents(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SavedEvent> saved = savedEventService.findByUser(user);
        model.addAttribute("savedEvents", saved);
        model.addAttribute("savedCount", saved.size());
        return "user/saved-events";
    }
}
