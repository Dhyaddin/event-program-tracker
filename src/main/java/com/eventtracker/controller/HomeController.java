package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * HomeController — landing page.
 * Feeds the home page with REAL approved events and category names from the database.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EventService eventService;

    @GetMapping("/")
    public String index(Model model) {
        List<Event> approved = eventService.findApprovedEvents();
        // Show up to 6 events as "featured"
        List<Event> featured = approved.size() > 6 ? approved.subList(0, 6) : approved;

        model.addAttribute("featuredEvents", featured);
        model.addAttribute("totalEvents", eventService.countApprovedEvents());
        model.addAttribute("categories", eventService.findDistinctCategories());
        return "index";
    }

}
