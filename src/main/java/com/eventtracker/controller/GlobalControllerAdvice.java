package com.eventtracker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * GlobalControllerAdvice
 *
 * Automatically injects shared model attributes into every controller response.
 * This avoids having to manually add common variables (like currentPage) in each controller.
 *
 * FRONTEND ONLY — No backend logic.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Adds the current request URI as 'currentPage' so Thymeleaf templates
     * can highlight active navigation links without using the deprecated #request object.
     *
     * Usage in Thymeleaf:
     *   th:classappend="${currentPage == '/'} ? 'active-nav' : ''"
     */
    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, Model model) {
        model.addAttribute("currentPage", request.getRequestURI());
    }

}
