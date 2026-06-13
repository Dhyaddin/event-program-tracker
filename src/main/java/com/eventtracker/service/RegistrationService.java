package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface RegistrationService {
    Registration registerForEvent(User user, Event event);
    Optional<Registration> findByUserAndEvent(User user, Event event);
    List<Registration> findByUser(User user);
    List<Registration> findByEvent(Event event);
    List<Registration> findByOrganizer(User organizer);
    void cancelRegistration(Long registrationId);
    boolean isUserRegistered(User user, Event event);
    long countRegistrations(Event event);
    long countAllRegistrations();
    void deleteRegistrationsForEvent(Event event);
    void deleteRegistrationsForUser(User user);

    /** Mark every active (REGISTERED) registration for this event as ATTENDED. */
    void markAttendedForEvent(Event event);
}