package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.ActivityLogService;
import com.eventtracker.service.EventCommentService;
import com.eventtracker.service.EventService;
import com.eventtracker.service.FeedbackService;
import com.eventtracker.service.FileStorageService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.SavedEventService;
import com.eventtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final SavedEventService savedEventService;
    private final FeedbackService feedbackService;
    private final ActivityLogService activityLogService;
    private final FileStorageService fileStorageService;
    private final EventCommentService eventCommentService;

    // ── List all events (public accessible) ─────────────────────────────────
    @GetMapping
    public String listEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String date,   // "upcoming" | "month"
            @RequestParam(required = false) String price,  // "free" | "paid"
            Model model) {

        // Start from the approved events only (the public should never see pending ones)
        List<Event> events;
        if (search != null && !search.isBlank()) {
            events = eventService.searchEvents(search).stream()
                    .filter(Event::isApproved)
                    .collect(Collectors.toList());
        } else if (category != null && !category.isBlank()) {
            events = eventService.findApprovedByCategory(category);
        } else {
            events = eventService.findApprovedEvents();
        }

        // ── Date filter ──
        if ("upcoming".equals(date)) {
            LocalDate today = LocalDate.now();
            events = events.stream()
                    .filter(e -> e.getEventDate() != null && !e.getEventDate().isBefore(today))
                    .collect(Collectors.toList());
        } else if ("month".equals(date)) {
            LocalDate today = LocalDate.now();
            events = events.stream()
                    .filter(e -> e.getEventDate() != null
                            && e.getEventDate().getYear() == today.getYear()
                            && e.getEventDate().getMonthValue() == today.getMonthValue())
                    .collect(Collectors.toList());
        }

        // ── Price filter ──
        if ("free".equals(price)) {
            events = events.stream().filter(Event::isFree).collect(Collectors.toList());
        } else if ("paid".equals(price)) {
            events = events.stream().filter(e -> !e.isFree()).collect(Collectors.toList());
        }

        model.addAttribute("events", events);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("date", date);
        model.addAttribute("price", price);
        // Distinct category names so the sidebar filter list is built from real data
        model.addAttribute("categories", eventService.findDistinctCategories());
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

        boolean isCompleted = event.getStatus() == Event.Status.COMPLETED;

        // ── Feedback / ratings (visible to everyone) ──
        model.addAttribute("feedbackList", feedbackService.findByEvent(event));
        model.addAttribute("averageRating", feedbackService.averageRating(event));
        model.addAttribute("feedbackCount", feedbackService.countByEvent(event));

        // ── Admin notes / comments (visible to admin + the owning organiser) ──
        model.addAttribute("comments", eventCommentService.findByEvent(event));

        // Defaults for anonymous visitors
        model.addAttribute("isRegistered", false);
        model.addAttribute("isSaved", false);
        model.addAttribute("canReview", false);
        model.addAttribute("hasReviewed", false);
        model.addAttribute("canRegister", false);  // only participants (USER role) can register
        model.addAttribute("canEdit", false);      // only the owning organiser
        model.addAttribute("canComment", false);   // only admins
        model.addAttribute("isOwner", false);

        // Show personalised status if the user is logged in
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                boolean registered = registrationService.isUserRegistered(user, event);
                model.addAttribute("isRegistered", registered);
                model.addAttribute("isSaved", savedEventService.isSaved(user, event));
                // Reviews are only allowed AFTER the event is completed, by a registered attendee
                model.addAttribute("canReview", registered && isCompleted);
                model.addAttribute("hasReviewed", feedbackService.hasReviewed(user, event));
                feedbackService.findByUserAndEvent(user, event)
                        .ifPresent(f -> model.addAttribute("myFeedback", f));
                // Registration is a participant action, and only while the event is live
                boolean live = event.getStatus() == Event.Status.UPCOMING
                        || event.getStatus() == Event.Status.ONGOING;
                model.addAttribute("canRegister", user.getRole() == User.Role.USER && live);
                // Edit only for the owning organiser
                boolean owner = isOwner(user, event);
                model.addAttribute("canEdit", owner);
                model.addAttribute("isOwner", owner);
                // Admins can leave a note for the organiser
                model.addAttribute("canComment", user.getRole() == User.Role.ADMIN);
            });
        }

        return "events/event-details";
    }

    // True only if this ORGANIZER owns the event. (Admins do NOT edit events.)
    private boolean canManage(User user, Event event) {
        return isOwner(user, event);
    }

    private boolean isOwner(User user, Event event) {
        if (user == null || event.getOrganizer() == null) return false;
        return event.getOrganizer().getId() != null
                && event.getOrganizer().getId().equals(user.getId());
    }

    // ── Show create event form (organizer/admin only) ────────────────────────
    @GetMapping("/create")
    public String createEventPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Block organisers who are still awaiting (or were denied) admin approval.
        User current = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (notApprovedOrganiser(current)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Your organiser account is awaiting admin approval. You cannot create events yet.");
            return "redirect:/dashboard";
        }

        model.addAttribute("event", new Event());
        return "events/create-event";
    }

    // ── Submit create event ──────────────────────────────────────────────────
    @PostMapping("/create")
    public String createEvent(
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "events/create-event";
        }

        User organizer = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        // Server-side guard: an unapproved organiser must not be able to create events
        // even by POSTing directly.
        if (notApprovedOrganiser(organizer)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Your organiser account is awaiting admin approval. You cannot create events yet.");
            return "redirect:/dashboard";
        }

        // Save the uploaded cover image (if any) and store its web path on the event
        boolean imageProvided = coverImage != null && !coverImage.isEmpty();
        boolean imageFailed = false;
        try {
            String imagePath = fileStorageService.storeImage(coverImage);
            if (imagePath != null) {
                event.setImageUrl(imagePath);
            }
        } catch (RuntimeException ex) {
            imageFailed = true;
        }

        event.setOrganizer(organizer);
        // Admin-created events go live immediately; organiser events await admin approval.
        boolean isAdmin = organizer.getRole() == User.Role.ADMIN;
        event.setApproved(isAdmin);
        eventService.createEvent(event);

        activityLogService.log("EVENT",
                organizer.getName() + " created event \"" + event.getTitle() + "\""
                        + (isAdmin ? "" : " (awaiting approval)"),
                organizer.getName());

        if (imageFailed) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Event created, but the cover image could not be uploaded (max 5MB). You can add it later via Edit.");
        } else {
            String base = isAdmin ? "Event created and published!"
                    : "Event submitted! It will appear publicly once an admin approves it.";
            if (imageProvided) base += " Cover image uploaded.";
            redirectAttributes.addFlashAttribute("successMessage", base);
        }
        return "redirect:/events";
    }

    // Helper: true if the account is an ORGANIZER that is not APPROVED.
    // ADMINs are never blocked.
    private boolean notApprovedOrganiser(User user) {
        return user != null
                && user.getRole() == User.Role.ORGANIZER
                && user.getStatus() != User.Status.APPROVED;
    }

    // ── Show edit event form ─────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editEventPage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        User current = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (!canManage(current, event)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only edit events you created.");
            return "redirect:/events/" + id;
        }

        model.addAttribute("event", event);
        return "events/edit-event";
    }

    // ── Submit edit event ────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String editEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event existing = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        // Ownership guard — an organiser can only edit their own events; admin can edit any.
        User current = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (!canManage(current, existing)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only edit events you created.");
            return "redirect:/events/" + id;
        }

        if (result.hasErrors()) {
            return "events/edit-event";
        }

        Event.Status oldStatus = existing.getStatus();

        // Copy only the editable fields. This preserves organizer, createdAt,
        // approval state and existing registrations.
        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setLocation(event.getLocation());
        existing.setEventDate(event.getEventDate());
        existing.setEventTime(event.getEventTime());
        existing.setCapacity(event.getCapacity());
        existing.setCategory(event.getCategory());
        existing.setPrice(event.getPrice());
        if (event.getStatus() != null) {
            existing.setStatus(event.getStatus());
        }
        // Only replace the cover image if a new one was uploaded; otherwise keep the old one.
        boolean imageFailed = false;
        try {
            String imagePath = fileStorageService.storeImage(coverImage);
            if (imagePath != null) {
                existing.setImageUrl(imagePath);
            }
        } catch (RuntimeException ex) {
            imageFailed = true;
        }

        eventService.updateEvent(existing);

        Event.Status newStatus = existing.getStatus();

        // ── Lifecycle side-effects ──
        if (newStatus == Event.Status.COMPLETED && oldStatus != Event.Status.COMPLETED) {
            // Everyone who was registered is now marked as attended (so they can review)
            registrationService.markAttendedForEvent(existing);
            activityLogService.log("EVENT",
                    "Event \"" + existing.getTitle() + "\" was marked completed", current != null ? current.getName() : "Organiser");
        } else if (newStatus == Event.Status.CANCELLED && oldStatus != Event.Status.CANCELLED) {
            long affected = registrationService.countRegistrations(existing);
            activityLogService.log("EVENT",
                    "Event \"" + existing.getTitle() + "\" was CANCELLED" + (affected > 0 ? " (" + affected + " registrant(s) affected)" : ""),
                    current != null ? current.getName() : "Organiser");
        }

        if (imageFailed) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Event saved, but the cover image could not be uploaded. Please try a smaller image (max 5MB).");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        }
        return "redirect:/events/" + id;
    }

    // ── Delete event ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id).orElse(null);
        User current = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (event == null) {
            return "redirect:/events";
        }
        // Ownership guard — organiser can only delete their own events; admin can delete any.
        if (!canManage(current, event)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only delete events you created.");
            return "redirect:/events/" + id;
        }
        // Remove all child rows first to avoid foreign-key violations
        registrationService.deleteRegistrationsForEvent(event);
        savedEventService.deleteSavedForEvent(event);
        feedbackService.deleteFeedbackForEvent(event);
        eventCommentService.deleteForEvent(event);
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

        // Only participants (USER role) may register. Organisers/admins manage events instead.
        if (user.getRole() != User.Role.USER) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only participant accounts can register for events.");
            return "redirect:/events/" + id;
        }

        try {
            registrationService.registerForEvent(user, event);
            activityLogService.log("REGISTRATION",
                    user.getName() + " registered for \"" + event.getTitle() + "\"", user.getName());
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

    // ── Save / unsave an event (bookmark toggle) ─────────────────────────────
    @PostMapping("/{id}/save")
    public String toggleSave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean nowSaved = savedEventService.toggleSave(user, event);
        redirectAttributes.addFlashAttribute("successMessage",
                nowSaved ? "Event saved to your list."
                         : "Event removed from your saved list.");
        return "redirect:/events/" + id;
    }

    // ── Submit feedback / rating (registered attendees only) ─────────────────
    @PostMapping("/{id}/feedback")
    public String submitFeedback(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only registered attendees may review the event
        if (!registrationService.isUserRegistered(user, event)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only review events you have registered for.");
            return "redirect:/events/" + id;
        }

        // Reviews are only allowed after the event is completed
        if (event.getStatus() != Event.Status.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only review an event after it has been completed.");
            return "redirect:/events/" + id;
        }

        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please choose a rating from 1 to 5 stars.");
            return "redirect:/events/" + id;
        }

        feedbackService.submitFeedback(user, event, rating, comment);
        activityLogService.log("FEEDBACK",
                user.getName() + " reviewed \"" + event.getTitle() + "\" (" + rating + "★)", user.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Thanks! Your review has been saved.");
        return "redirect:/events/" + id;
    }

    // ── Admin leaves a note for the organiser (admins cannot edit events directly) ──
    @PostMapping("/{id}/comment")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String message,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only admins may leave notes for organisers
        if (user.getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only admins can leave notes on events.");
            return "redirect:/events/" + id;
        }
        if (message == null || message.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a note before submitting.");
            return "redirect:/events/" + id;
        }

        eventCommentService.addComment(event, user, message.trim());
        activityLogService.log("APPROVAL",
                "Admin left a note on \"" + event.getTitle() + "\" for " +
                        (event.getOrganizer() != null ? event.getOrganizer().getName() : "the organiser"),
                user.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Your note has been sent to the organiser.");
        return "redirect:/events/" + id;
    }
}
