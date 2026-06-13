package com.eventtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * EventComment — a note left by an admin on an event, addressed to the organiser.
 * Admins cannot edit events directly; instead they leave comments so the organiser
 * can consider the suggested changes and decide whether to apply them.
 */
@Entity
@Table(name = "event_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
