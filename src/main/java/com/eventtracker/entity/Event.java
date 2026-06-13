package com.eventtracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @NotNull(message = "Event date is required")
    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(nullable = false)
    private Integer capacity = 0;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UPCOMING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Double price = 0.0;

    // Admin approval: organiser-created events start unapproved (hidden from public)
    // until an admin approves them. Admin-created and seeded events are approved.
    // Nullable so adding the column to an existing database does not fail.
    @Column
    private Boolean approved;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.UPCOMING;
        if (this.price == null) this.price = 0.0;
        if (this.capacity == null) this.capacity = 0;
        if (this.approved == null) this.approved = false;
    }

    // Convenience helper for templates: treats null as not approved.
    public boolean isApproved() {
        return Boolean.TRUE.equals(this.approved);
    }

    // Convenience helper — used in Thymeleaf with th:text="${event.free ? 'Free' : event.price}"
    public boolean isFree() {
        return this.price == null || this.price == 0.0;
    }

    public enum Status {
        UPCOMING, ONGOING, COMPLETED, CANCELLED
    }
}