package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Feedback;
import com.eventtracker.entity.User;
import com.eventtracker.repository.FeedbackRepository;
import com.eventtracker.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public Feedback submitFeedback(User user, Event event, int rating, String comment) {
        // One review per user per event: update the existing review if there is one.
        Feedback feedback = feedbackRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> Feedback.builder().user(user).event(event).build());
        feedback.setRating(rating);
        feedback.setComment(comment);
        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> findByEvent(Event event) {
        return feedbackRepository.findByEventOrderByCreatedAtDesc(event);
    }

    @Override
    public Optional<Feedback> findByUserAndEvent(User user, Event event) {
        return feedbackRepository.findByUserAndEvent(user, event);
    }

    @Override
    public boolean hasReviewed(User user, Event event) {
        return feedbackRepository.existsByUserAndEvent(user, event);
    }

    @Override
    public long countByEvent(Event event) {
        return feedbackRepository.countByEvent(event);
    }

    @Override
    public double averageRating(Event event) {
        Double avg = feedbackRepository.averageRatingForEvent(event);
        if (avg == null) return 0.0;
        return Math.round(avg * 10.0) / 10.0;
    }

    @Override
    @Transactional
    public void deleteFeedbackForEvent(Event event) {
        feedbackRepository.deleteByEvent(event);
    }

    @Override
    @Transactional
    public void deleteFeedbackForUser(User user) {
        feedbackRepository.deleteByUser(user);
    }
}
