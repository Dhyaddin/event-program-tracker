package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.SavedEvent;
import com.eventtracker.entity.User;
import com.eventtracker.repository.SavedEventRepository;
import com.eventtracker.service.SavedEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedEventServiceImpl implements SavedEventService {

    private final SavedEventRepository savedEventRepository;

    @Override
    @Transactional
    public boolean toggleSave(User user, Event event) {
        return savedEventRepository.findByUserAndEvent(user, event)
                .map(existing -> {
                    // Already saved → remove the bookmark
                    savedEventRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    // Not saved → create the bookmark
                    savedEventRepository.save(SavedEvent.builder()
                            .user(user)
                            .event(event)
                            .build());
                    return true;
                });
    }

    @Override
    public boolean isSaved(User user, Event event) {
        return savedEventRepository.existsByUserAndEvent(user, event);
    }

    @Override
    public List<SavedEvent> findByUser(User user) {
        return savedEventRepository.findByUserOrderBySavedAtDesc(user);
    }

    @Override
    public long countByUser(User user) {
        return savedEventRepository.countByUser(user);
    }

    @Override
    @Transactional
    public void deleteSavedForEvent(Event event) {
        savedEventRepository.deleteByEvent(event);
    }

    @Override
    @Transactional
    public void deleteSavedForUser(User user) {
        savedEventRepository.deleteByUser(user);
    }
}
