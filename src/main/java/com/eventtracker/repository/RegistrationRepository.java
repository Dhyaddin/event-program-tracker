package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByUser(User user);

    List<Registration> findByEvent(Event event);

    // All registrations across every event owned by a given organiser
    List<Registration> findByEvent_Organizer(User organizer);

    Optional<Registration> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    long countByEvent(Event event);

    // Used before deleting an event/user to avoid foreign-key violations
    void deleteByEvent(Event event);

    void deleteByUser(User user);
}