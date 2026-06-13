package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.EventComment;
import com.eventtracker.entity.User;

import java.util.List;

public interface EventCommentService {

    EventComment addComment(Event event, User author, String message);

    List<EventComment> findByEvent(Event event);

    long countByEvent(Event event);

    void deleteForEvent(Event event);
}
