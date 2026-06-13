package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface EventService {
    Event createEvent(Event event);
    Optional<Event> findById(Long id);
    List<Event> findAllEvents();
    List<Event> findEventsByOrganizer(User organizer);
    List<Event> findEventsByStatus(Event.Status status);
    List<Event> searchEvents(String keyword);
    List<Event> findByCategory(String category);
    List<String> findDistinctCategories();
    Event updateEvent(Event event);
    void deleteEvent(Long id);
    long countAllEvents();

    // ── Approval workflow ──
    List<Event> findApprovedEvents();
    List<Event> findApprovedByCategory(String category);
    List<Event> findPendingEvents();
    long countPendingEvents();
    long countApprovedEvents();
    void approveEvent(Long id);
}