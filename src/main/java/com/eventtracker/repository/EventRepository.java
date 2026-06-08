package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByStatus(Event.Status status);

    List<Event> findByCategory(String category);

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findAllByOrderByEventDateAsc();
}