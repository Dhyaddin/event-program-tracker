package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.repository.EventRepository;
import com.eventtracker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findAllEvents() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    @Override
    public List<Event> findEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    @Override
    public List<Event> findEventsByStatus(Event.Status status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public List<Event> findByCategory(String category) {
        return eventRepository.findByCategoryOrderByEventDateAsc(category);
    }

    @Override
    public List<String> findDistinctCategories() {
        return eventRepository.findDistinctCategories();
    }

    @Override
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public long countAllEvents() {
        return eventRepository.count();
    }

    // ── Approval workflow ────────────────────────────────────────────────────

    @Override
    public List<Event> findApprovedEvents() {
        // Public listing: approved AND still live (hide completed & cancelled events)
        return eventRepository.findByApprovedTrueOrderByEventDateAsc().stream()
                .filter(EventServiceImpl::isLive)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Event> findApprovedByCategory(String category) {
        return eventRepository.findByApprovedTrueAndCategoryOrderByEventDateAsc(category).stream()
                .filter(EventServiceImpl::isLive)
                .collect(java.util.stream.Collectors.toList());
    }

    // A "live" event is one the public can still see and register for.
    private static boolean isLive(Event e) {
        return e.getStatus() == Event.Status.UPCOMING || e.getStatus() == Event.Status.ONGOING;
    }

    @Override
    public List<Event> findPendingEvents() {
        return eventRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    @Override
    public long countPendingEvents() {
        return eventRepository.countByApprovedFalse();
    }

    @Override
    public long countApprovedEvents() {
        return eventRepository.countByApprovedTrue();
    }

    @Override
    public void approveEvent(Long id) {
        eventRepository.findById(id).ifPresent(event -> {
            event.setApproved(true);
            eventRepository.save(event);
        });
    }
}