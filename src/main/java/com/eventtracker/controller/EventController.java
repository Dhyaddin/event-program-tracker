package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    // PLACEHOLDER — TEAMMATE IMPLEMENTATION AREA
    //
    // @GetMapping
    // public String eventList(Model model) {
    //     model.addAttribute("events", eventService.findAll());
    //     return "events/event-list";
    // }
    //
    // @PostMapping("/create")
    // public String createEvent(@ModelAttribute EventDto dto) {
    //     eventService.create(dto);
    //     return "redirect:/events";
    // }
    //
    // @PostMapping("/{id}/edit")
    // public String editEvent(@PathVariable Long id, @ModelAttribute EventDto dto) {
    //     eventService.update(id, dto);
    //     return "redirect:/events/" + id;
    // }
    //
    // @PostMapping("/{id}/delete")
    // public String deleteEvent(@PathVariable Long id) {
    //     eventService.delete(id);
    //     return "redirect:/events";
    // }
    // ================================================

}
