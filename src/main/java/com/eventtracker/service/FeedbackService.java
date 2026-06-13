package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Feedback;
import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {

    /** Create a new review, or update the user's existing review for this event. */
    Feedback submitFeedback(User user, Event event, int rating, String comment);

    List<Feedback> findByEvent(Event event);

    Optional<Feedback> findByUserAndEvent(User user, Event event);

    boolean hasReviewed(User user, Event event);

    long countByEvent(Event event);

    /** Average rating rounded to one decimal place, or 0.0 when there is no feedback. */
    double averageRating(Event event);

    void deleteFeedbackForEvent(Event event);

    void deleteFeedbackForUser(User user);
}
