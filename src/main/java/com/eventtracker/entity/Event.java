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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.UPCOMING;
        if (this.price == null) this.price = 0.0;
        if (this.capacity == null) this.capacity = 0;
    }

    // Convenience helper — used in Thymeleaf with th:text="${event.free ? 'Free' : event.price}"
    public boolean isFree() {
        return this.price == null || this.price == 0.0;
    }

    public enum Status {
        UPCOMING, ONGOING, COMPLETED, CANCELLED
    }
}