package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.ActivityLogService;
import com.eventtracker.service.EventCommentService;
import com.eventtracker.service.EventService;
import com.eventtracker.service.FeedbackService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.SavedEventService;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminPagesController
 * Routes: Organiser Approvals, Event Approvals, Manage Users (read-only).
 *
 * Wired to the database so every admin page reflects real data.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPagesController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SavedEventService savedEventService;
    private final FeedbackService feedbackService;
    private final ActivityLogService activityLogService;
    private final EventCommentService eventCommentService;

    // ── Organiser Approvals ──────────────────────────────────────────────────
    @GetMapping("/approvals")
    public String approvals(Model model) {
        List<User> pending = userService.findPendingOrganisers();
        model.addAttribute("pendingOrganisers", pending);
        model.addAttribute("pendingCount", pending.size());
        return "admin/approvals";
    }

    @PostMapping("/approvals/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        userService.approveOrganiser(id);
        userService.findById(id).ifPresent(u ->
                activityLogService.log("APPROVAL", "Organiser \"" + u.getName() + "\" was approved", "Admin"));
        ra.addFlashAttribute("successMessage", "Organiser approved. They can now create events.");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/approvals/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        userService.rejectOrganiser(id);
        userService.findById(id).ifPresent(u ->
                activityLogService.log("APPROVAL", "Organiser \"" + u.getName() + "\" was rejected", "Admin"));
        ra.addFlashAttribute("successMessage", "Organiser request rejected.");
        return "redirect:/admin/approvals";
    }

    // ── Event Approvals (pending events created by organisers) ───────────────
    @GetMapping("/event-approvals")
    public String eventApprovals(Model model) {
        List<Event> pending = eventService.findPendingEvents();
        model.addAttribute("pendingEvents", pending);
        model.addAttribute("pendingEventCount", pending.size());
        return "admin/event-approvals";
    }

    @PostMapping("/event-approvals/{id}/approve")
    public String approveEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.approveEvent(id);
        eventService.findById(id).ifPresent(e ->
                activityLogService.log("EVENT", "Event \"" + e.getTitle() + "\" was approved & published", "Admin"));
        ra.addFlashAttribute("successMessage", "Event approved and published.");
        return "redirect:/admin/event-approvals";
    }

    @PostMapping("/event-approvals/{id}/reject")
    public String rejectEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.findById(id).ifPresent(event -> {
            String title = event.getTitle();
            // Remove child rows first to avoid FK violations, then delete the event
            registrationService.deleteRegistrationsForEvent(event);
            savedEventService.deleteSavedForEvent(event);
            feedbackService.deleteFeedbackForEvent(event);
            eventCommentService.deleteForEvent(event);
            eventService.deleteEvent(event.getId());
            activityLogService.log("EVENT", "Pending event \"" + title + "\" was rejected & removed", "Admin");
        });
        ra.addFlashAttribute("successMessage", "Event rejected and removed.");
        return "redirect:/admin/event-approvals";
    }

    // ── Manage Users (read-only table) ───────────────────────────────────────
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("organiserCount",
                allUsers.stream().filter(u -> u.getRole() == User.Role.ORGANIZER).count());
        model.addAttribute("adminCount",
                allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count());
        model.addAttribute("regularUserCount",
                allUsers.stream().filter(u -> u.getRole() == User.Role.USER).count());
        return "admin/manage-users";
    }

    // ── All Events (platform-wide) ───────────────────────────────────────────
    @GetMapping("/events")
    public String allEvents(Model model) {
        List<Event> allEvents = eventService.findAllEvents();
        model.addAttribute("allPlatformEvents", allEvents);
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("approvedEventCount", eventService.countApprovedEvents());
        model.addAttribute("pendingEventCount", eventService.countPendingEvents());
        model.addAttribute("regCounts", buildRegCounts(allEvents));
        return "admin/all-events";
    }

    // Build an eventId -> registrationCount map for the templates
    private Map<Long, Long> buildRegCounts(List<Event> events) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Event e : events) {
            counts.put(e.getId(), registrationService.countRegistrations(e));
        }
        return counts;
    }
}
