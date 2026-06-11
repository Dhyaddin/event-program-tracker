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
}