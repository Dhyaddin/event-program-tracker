package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.EventComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {

    List<EventComment> findByEventOrderByCreatedAtDesc(Event event);

    long countByEvent(Event event);

    // Used before deleting an event to avoid foreign-key violations
    void deleteByEvent(Event event);
}
