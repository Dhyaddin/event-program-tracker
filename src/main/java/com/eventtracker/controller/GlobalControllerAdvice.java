package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.EventService;
import com.eventtracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * GlobalControllerAdvice
 *
 * Automatically injects shared model attributes into every controller response.
 * This avoids having to manually add common variables in each controller.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;
    private final EventService eventService;

    /**
     * Adds the current request URI as 'currentPage' (used to highlight nav links),
     * and the logged-in account as 'currentUser' (used by fragments to show the
     * avatar/name and to gate the organiser "Create Event" action on approval status).
     */
    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, Model model) {
        model.addAttribute("currentPage", request.getRequestURI());
        // Real category names for the navbar dropdown (built from existing events)
        model.addAttribute("navCategories", eventService.findDistinctCategories());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));

        if (isAuthenticated) {
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            // Organisers awaiting (or denied) admin approval cannot create events.
            boolean pendingOrganiser = currentUser != null
                    && currentUser.getRole() == User.Role.ORGANIZER
                    && currentUser.getStatus() != User.Status.APPROVED;
            model.addAttribute("isPendingOrganiser", pendingOrganiser);
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("isPendingOrganiser", false);
        }
    }

}
