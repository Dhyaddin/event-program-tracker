package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.SavedEvent;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedEventRepository extends JpaRepository<SavedEvent, Long> {

    List<SavedEvent> findByUserOrderBySavedAtDesc(User user);

    Optional<SavedEvent> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    long countByUser(User user);

    // Used before deleting an event/user to avoid foreign-key violations
    void deleteByEvent(Event event);

    void deleteByUser(User user);
}
