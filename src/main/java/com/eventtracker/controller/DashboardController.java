package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.EventService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", currentUser);

        return switch (currentUser.getRole()) {

            case ADMIN -> {
                model.addAttribute("totalUsers", userService.countAllUsers());
                model.addAttribute("totalEvents", eventService.countAllEvents());
                model.addAttribute("allEvents", eventService.findAllEvents());
                model.addAttribute("allUsers", userService.findAllUsers());
                yield "dashboard/dashboard-admin";
            }

            case ORGANIZER -> {
                List<Event> myEvents = eventService.findEventsByOrganizer(currentUser);
                model.addAttribute("myEvents", myEvents);
                model.addAttribute("totalMyEvents", myEvents.size());
                model.addAttribute("upcomingEvents",
                        eventService.findEventsByStatus(Event.Status.UPCOMING));
                yield "dashboard/dashboard-organiser";
            }

            default -> {  // USER
                var registrations = registrationService.findByUser(currentUser);
                model.addAttribute("registrations", registrations);
                model.addAttribute("totalRegistered", registrations.size());
                model.addAttribute("upcomingEvents",
                        eventService.findEventsByStatus(Event.Status.UPCOMING));
                yield "dashboard/dashboard-user";
            }
        };
    }

    // ── Role-specific URL aliases — security rules enforce who can access each ──
    @GetMapping("/user")
    public String userDashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        return dashboard(ud, model);
    }

    @GetMapping("/organiser")
    public String organiserDashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        return dashboard(ud, model);
    }

    @GetMapping("/admin")
    public String adminDashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        return dashboard(ud, model);
    }
}