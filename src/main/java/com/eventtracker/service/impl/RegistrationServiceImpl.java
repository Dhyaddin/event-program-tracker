package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;
import com.eventtracker.repository.RegistrationRepository;
import com.eventtracker.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;

    @Override
    public Registration registerForEvent(User user, Event event) {
        if (isUserRegistered(user, event)) {
            throw new RuntimeException("You are already registered for this event.");
        }
        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .status(Registration.Status.REGISTERED)
                .build();
        return registrationRepository.save(registration);
    }

    @Override
    public Optional<Registration> findByUserAndEvent(User user, Event event) {
        return registrationRepository.findByUserAndEvent(user, event);
    }

    @Override
    public List<Registration> findByUser(User user) {
        return registrationRepository.findByUser(user);
    }

    @Override
    public List<Registration> findByEvent(Event event) {
        return registrationRepository.findByEvent(event);
    }

    @Override
    public void cancelRegistration(Long registrationId) {
        registrationRepository.findById(registrationId).ifPresent(reg -> {
            reg.setStatus(Registration.Status.CANCELLED);
            registrationRepository.save(reg);
        });
    }

    @Override
    public boolean isUserRegistered(User user, Event event) {
        return registrationRepository.existsByUserAndEvent(user, event);
    }

    @Override
    public long countRegistrations(Event event) {
        return registrationRepository.countByEvent(event);
    }
}