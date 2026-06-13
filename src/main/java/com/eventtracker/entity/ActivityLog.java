package com.eventtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ActivityLog — a record of a notable system action (account registered, event created,
 * registration made, organiser approved, etc.). Displayed on the Admin dashboard as the
 * live "System Activity" feed so the admin can monitor what users and organisers are doing.
 */
@Entity
@Table(name = "activity_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short machine type used to pick an icon/colour in the UI, e.g. USER, EVENT, REGISTRATION, APPROVAL.
    @Column(nullable = false)
    private String type;

    // Human-readable message, e.g. "Siti registered for Tech Conference".
    @Column(nullable = false, length = 500)
    private String message;

    // Who triggered it (display name), may be null for system events.
    private String actor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
