package com.eventtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SavedEvent — a user's bookmark of an event ("save for later").
 *
 * Join/bridge table between users and events, separate from Registration:
 * saving an event does NOT register you for it.
 * UNIQUE(user_id, event_id) prevents the same user saving the same event twice.
 */
@Entity
@Table(
        name = "saved_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}
