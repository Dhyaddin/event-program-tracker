package com.eventtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserPagesController
 * Routes: My Registrations, Saved Events
 *
 * FRONTEND ONLY — No service or repository logic.
 */
@Controller
public class UserPagesController {

    @GetMapping("/my-registrations")
    public String myRegistrations() {
        return "user/my-registrations";
    }

    @GetMapping("/my-saved")
    public String savedEvents() {
        return "user/saved-events";
    }

    // ================================================
    // TEAMMATE IMPLEMENTATION AREA
    // Inject UserService / RegistrationService
    // ================================================
}
