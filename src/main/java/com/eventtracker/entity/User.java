package com.eventtracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Account approval state.
    // USERs are auto-APPROVED on registration. New ORGANIZERs start as PENDING
    // until an admin approves (or rejects) them.
    // Column is nullable so that adding it to an existing database (ddl-auto=update)
    // does not fail on rows created before this field existed.
    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    private String phone;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
        // Default approval state if none was set:
        // organizers need approval, everyone else is approved immediately.
        if (this.status == null) {
            this.status = (this.role == Role.ORGANIZER) ? Status.PENDING : Status.APPROVED;
        }
    }

    // Convenience helper for templates / controllers.
    public boolean isApproved() {
        return this.status == Status.APPROVED;
    }

    public enum Role {
        USER, ORGANIZER, ADMIN
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}