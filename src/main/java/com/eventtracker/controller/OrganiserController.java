package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OrganiserController
 * Routes: My Events, Registrations, Analytics
 *
 * FRONTEND ONLY — No service or repository logic.
 */
@Controller
@RequestMapping("/organiser")
public class OrganiserController {

    @GetMapping("/my-events")
    public String myEvents() {
        return "organiser/my-events";
    }

    @GetMapping("/registrations")
    public String registrations() {
        return "organiser/registrations";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "organiser/analytics";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // Inject EventService / RegistrationService
    // ================================================
}
