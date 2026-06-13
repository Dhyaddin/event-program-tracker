package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.SavedEvent;
import com.eventtracker.entity.User;

import java.util.List;

public interface SavedEventService {

    /** Toggle a bookmark: saves if not saved, removes if already saved. Returns true if now saved. */
    boolean toggleSave(User user, Event event);

    boolean isSaved(User user, Event event);

    List<SavedEvent> findByUser(User user);

    long countByUser(User user);

    void deleteSavedForEvent(Event event);

    void deleteSavedForUser(User user);
}
