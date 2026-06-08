package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.EventService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;

    // ── List all events (public accessible) ─────────────────────────────────
    @GetMapping
    public String listEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model) {

        var events = (search != null && !search.isBlank())
                ? eventService.searchEvents(search)
                : eventService.findAllEvents();

        model.addAttribute("events", events);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        return "events/event-list";
    }

    // ── Event detail page ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String eventDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        model.addAttribute("event", event);
        model.addAttribute("registrationCount", registrationService.countRegistrations(event));

        // Show registration status if user is logged in
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user ->
                    model.addAttribute("isRegistered",
                            registrationService.isUserRegistered(user, event))
            );
        }

        return "events/event-details";
    }

    // ── Show create event form (organizer/admin only) ────────────────────────
    @GetMapping("/create")
    public String createEventPage(Model model) {
        model.addAttribute("event", new Event());
        return "events/create-event";
    }

    // ── Submit create event ──────────────────────────────────────────────────
    @PostMapping("/create")
    public String createEvent(
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "events/create-event";
        }

        User organizer = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        event.setOrganizer(organizer);
        eventService.createEvent(event);

        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/events";
    }

    // ── Show edit event form ─────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editEventPage(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        model.addAttribute("event", event);
        return "events/edit-event";
    }

    // ── Submit edit event ────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String editEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "events/edit-event";
        }

        event.setId(id);
        eventService.updateEvent(event);

        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/events/" + id;
    }

    // ── Delete event ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Event has been deleted.");
        return "redirect:/events";
    }

    // ── Register for an event ────────────────────────────────────────────────
    @PostMapping("/{id}/register")
    public String registerForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            registrationService.registerForEvent(user, event);
            redirectAttributes.addFlashAttribute("successMessage",
                    "You have successfully registered for \"" + event.getTitle() + "\"!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/events/" + id;
    }

    // ── Cancel registration ──────────────────────────────────────────────────
    @PostMapping("/{id}/cancel-registration")
    public String cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        registrationService.findByUserAndEvent(user, event)
                .ifPresent(reg -> registrationService.cancelRegistration(reg.getId()));

        redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled.");
        return "redirect:/events/" + id;
    }
}
