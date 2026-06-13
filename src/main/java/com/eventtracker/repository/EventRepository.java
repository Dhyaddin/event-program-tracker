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

    List<Event> findByCategoryOrderByEventDateAsc(String category);

    // ── Approval-aware queries (public listings show approved events only) ──
    List<Event> findByApprovedTrueOrderByEventDateAsc();

    List<Event> findByApprovedTrueAndCategoryOrderByEventDateAsc(String category);

    List<Event> findByApprovedFalseOrderByCreatedAtDesc();

    long countByApprovedFalse();

    long countByApprovedTrue();

    // Distinct, non-null category names for building filter chips
    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT e.category FROM Event e WHERE e.category IS NOT NULL ORDER BY e.category")
    List<String> findDistinctCategories();
}