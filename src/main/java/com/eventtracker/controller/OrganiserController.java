package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OrganiserController
 * Routes: My Events, Registrations, Analytics
 *
 * Wired to the database — all data is scoped to the logged-in organiser.
 */
@Controller
@RequestMapping("/organiser")
@RequiredArgsConstructor
public class OrganiserController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    // ── My Events ────────────────────────────────────────────────────────────
    @GetMapping("/my-events")
    public String myEvents(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organiser = currentUser(ud);
        List<Event> myEvents = eventService.findEventsByOrganizer(organiser);

        model.addAttribute("myEvents", myEvents);
        model.addAttribute("totalMyEvents", myEvents.size());
        model.addAttribute("regCounts", buildRegCounts(myEvents));
        model.addAttribute("totalRegistrations",
                registrationService.findByOrganizer(organiser).size());
        return "organiser/my-events";
    }

    // ── Registrations across all my events ───────────────────────────────────
    @GetMapping("/registrations")
    public String registrations(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organiser = currentUser(ud);
        List<Registration> allRegistrations = registrationService.findByOrganizer(organiser);
        model.addAttribute("allRegistrations", allRegistrations);
        model.addAttribute("totalRegistrations", allRegistrations.size());
        return "organiser/registrations";
    }

    // ── Analytics (per-event registration stats) ─────────────────────────────
    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organiser = currentUser(ud);
        List<Event> myEvents = eventService.findEventsByOrganizer(organiser);
        model.addAttribute("myEvents", myEvents);
        model.addAttribute("regCounts", buildRegCounts(myEvents));
        model.addAttribute("totalRegistrations",
                registrationService.findByOrganizer(organiser).size());
        return "organiser/analytics";
    }

    private User currentUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Organiser not found"));
    }

    private Map<Long, Long> buildRegCounts(List<Event> events) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Event e : events) {
            counts.put(e.getId(), registrationService.countRegistrations(e));
        }
        return counts;
    }
}
