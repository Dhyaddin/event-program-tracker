package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * EventController
 * Routes: Event List, Event Detail, Create Event, Edit Event
 *
 * FRONTEND ONLY — No service or repository logic.
 * All event data is dummy/placeholder in Thymeleaf templates.
 */
@Controller
@RequestMapping("/events")
public class EventController {

    @GetMapping
    public String eventList() {
        return "events/event-list";
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id) {
        return "events/event-details";
    }

    @GetMapping("/create")
    public String createEvent() {
        return "events/create-event";
    }

    @GetMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id) {
        return "events/edit-event";
    }

    // ================================================
    // FRONTEND STUBS — Prevents 405 Method Not Allowed
    // Redirect back so forms don't crash during UI demo.
    // Teammates replace these with real service calls.
    // ================================================

    @PostMapping("/create")
    public String createEventPost() {
        // PLACEHOLDER — eventService.create(dto) by teammate
        return "redirect:/events";
    }

    @PostMapping("/{id}/edit")
    public String editEventPost(@PathVariable Long id) {
        // PLACEHOLDER — eventService.update(id, dto) by teammate
        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteEventPost(@PathVariable Long id) {
        // PLACEHOLDER — eventService.delete(id) by teammate
        return "redirect:/events";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // Inject EventService and replace the stubs above.
    // ================================================

}
