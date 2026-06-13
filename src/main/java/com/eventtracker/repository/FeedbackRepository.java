package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Feedback;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEventOrderByCreatedAtDesc(Event event);

    Optional<Feedback> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    long countByEvent(Event event);

    // Average star rating for an event (null when there is no feedback yet)
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event = :event")
    Double averageRatingForEvent(@Param("event") Event event);

    // Used before deleting an event/user to avoid foreign-key violations
    void deleteByEvent(Event event);

    void deleteByUser(User user);
}
