package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.EventComment;
import com.eventtracker.entity.User;
import com.eventtracker.repository.EventCommentRepository;
import com.eventtracker.service.EventCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCommentServiceImpl implements EventCommentService {

    private final EventCommentRepository eventCommentRepository;

    @Override
    public EventComment addComment(Event event, User author, String message) {
        return eventCommentRepository.save(EventComment.builder()
                .event(event)
                .author(author)
                .message(message)
                .build());
    }

    @Override
    public List<EventComment> findByEvent(Event event) {
        return eventCommentRepository.findByEventOrderByCreatedAtDesc(event);
    }

    @Override
    public long countByEvent(Event event) {
        return eventCommentRepository.countByEvent(event);
    }

    @Override
    @Transactional
    public void deleteForEvent(Event event) {
        eventCommentRepository.deleteByEvent(event);
    }
}
