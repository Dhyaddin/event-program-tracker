# 🔧 Backend & Database Team — Complete Implementation Guide
## Event & Program Tracker | CSC3402 Group Project

> **Document Owner:** Frontend Developer
> **Target Audience:** Backend & Database Team Members
> **Your Git Branch:** Create and use `backend-db` branch (branch off `main`)
> **Frontend Branch:** `frontend-ui` (do NOT touch this branch)
> **Database:** MySQL — `event_tracker_db` (already configured in `application.properties`)

---

## 📋 Table of Contents

1. [Your Role vs What Frontend Has Done](#1-your-role-vs-what-frontend-has-done)
2. [‼️ Fix pom.xml FIRST — Critical](#2-fix-pomxml-first--critical)
3. [🗄️ Database Design — 3 Tables](#3-database-design--3-tables)
4. [📁 Your File Structure](#4-your-file-structure)
5. [🚀 Phase-by-Phase Implementation](#5-phase-by-phase-implementation)
   - [Phase 1 — Entity Classes (Models)](#phase-1--entity-classes-models)
   - [Phase 2 — Repository Layer](#phase-2--repository-layer)
   - [Phase 3 — Service Layer](#phase-3--service-layer)
   - [Phase 4 — Spring Security](#phase-4--spring-security)
   - [Phase 5 — Controller Integration](#phase-5--controller-integration)
   - [Phase 6 — Data Seeder (Test Data)](#phase-6--data-seeder-test-data)
6. [🔗 Frontend Integration Points (Model Attributes)](#6-frontend-integration-points-model-attributes)
7. [✅ Completion Checklist](#7-completion-checklist)
8. [⚠️ Team Rules & Communication](#8-team-rules--communication)

---

## 1. Your Role vs What Frontend Has Done

### ✅ What the Frontend Developer Has Already Done
- Spring Boot project initialized and pushed to GitHub
- Maven, IntelliJ, and local environment fully configured
- CSS design system created (`variables.css`, `global.css`, `auth.css`, `dashboard.css`, `event.css`, `responsive.css`)
- All HTML templates being built in `src/main/resources/templates/`
- Frontend-only controllers created (routing/page rendering only — no service logic yet)
- `application.properties` configured with MySQL connection settings
- GitHub repository `event-program-tracker` connected

### 🎯 What YOUR Team Must Build
- Fix broken test dependencies in `pom.xml`
- Design MySQL database (3 tables minimum per assignment)
- Implement all JPA Entity classes
- Implement all Repository interfaces
- Implement all Service interfaces + implementations
- Implement Spring Security (login, registration, role-based access)
- Fill in controller methods with real backend/service logic
- Create a data seeder with test accounts for the whole team

### 🚫 DO NOT TOUCH (Frontend Territory)
- Any file inside `src/main/resources/templates/` (HTML files)
- Any file inside `src/main/resources/static/css/` (CSS files)
- Any file inside `src/main/resources/static/js/` (JS files)
- `application.properties` (already configured correctly)

### ⚠️ Assignment Note
The assignment brief specifies Oracle DBMS, but this project uses **MySQL** for convenience. If your lecturer requires Oracle, switch the `pom.xml` dependency from `mysql-connector-j` to the Oracle JDBC driver and update `application.properties` accordingly. Confirm this with your team.

---

## 2. ‼️ Fix pom.xml FIRST — Critical

The current `pom.xml` has **invalid test dependencies** that do not exist in Spring Boot. This will cause the build to fail. Fix this before doing anything else.

### ❌ REMOVE all of these (they don't exist):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

### ✅ REPLACE with these two (correct):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

After editing, run in terminal:
```bash
mvn clean install
```
It must build successfully (BUILD SUCCESS) before proceeding.

---

## 3. Database Design — 3 Tables

> Hibernate will **auto-generate** the tables because `application.properties` has `spring.jpa.hibernate.ddl-auto=update`. You do NOT need to manually run SQL — just create the database once.

### Step 1: Create the database manually (one-time only)

Open MySQL Workbench or terminal and run:
```sql
CREATE DATABASE IF NOT EXISTS event_tracker_db;
```

That's it. Hibernate creates all tables from your entity classes.

---

### Table 1: `users`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary Key, Auto-increment |
| `name` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | BCrypt hashed — NEVER plain text |
| `role` | ENUM | `USER` / `ORGANIZER` / `ADMIN` |
| `phone` | VARCHAR(20) | Optional |
| `profile_picture` | VARCHAR(255) | Optional (file path or URL) |
| `created_at` | DATETIME | Auto-set on insert |

---

### Table 2: `events`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary Key, Auto-increment |
| `title` | VARCHAR(200) | NOT NULL |
| `description` | TEXT | Optional |
| `location` | VARCHAR(200) | Optional |
| `event_date` | DATE | NOT NULL |
| `event_time` | TIME | Optional |
| `capacity` | INT | Default 0 |
| `category` | VARCHAR(100) | e.g. Technology, Sports |
| `status` | ENUM | `UPCOMING` / `ONGOING` / `COMPLETED` / `CANCELLED` |
| `organizer_id` | BIGINT (FK) | References `users.id` |
| `image_url` | VARCHAR(255) | Optional |
| `price` | DOUBLE | 0.0 = Free |
| `created_at` | DATETIME | Auto-set on insert |

---

### Table 3: `registrations`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary Key, Auto-increment |
| `user_id` | BIGINT (FK) | References `users.id` |
| `event_id` | BIGINT (FK) | References `events.id` |
| `registration_date` | DATETIME | Auto-set on insert |
| `status` | ENUM | `REGISTERED` / `CANCELLED` / `ATTENDED` |

> UNIQUE constraint on `(user_id, event_id)` — one user can only register once per event.

---

## 4. Your File Structure

Create ONLY these files. All other existing files belong to the frontend developer.

```
src/main/java/com/eventtracker/
│
├── config/
│   ├── SecurityConfig.java             ← NEW — Spring Security setup
│   └── DataInitializer.java            ← NEW — seeds test data on startup
│
├── entity/
│   ├── User.java                       ← NEW — User JPA entity
│   ├── Event.java                      ← NEW — Event JPA entity
│   └── Registration.java              ← NEW — Registration JPA entity
│
├── repository/
│   ├── UserRepository.java             ← NEW
│   ├── EventRepository.java            ← NEW
│   └── RegistrationRepository.java     ← NEW
│
├── security/
│   └── CustomUserDetailsService.java   ← NEW — loads user for Spring Security
│
└── service/
    ├── UserService.java                ← NEW — interface
    ├── EventService.java               ← NEW — interface
    ├── RegistrationService.java        ← NEW — interface
    └── impl/
        ├── UserServiceImpl.java        ← NEW — implementation
        ├── EventServiceImpl.java       ← NEW — implementation
        └── RegistrationServiceImpl.java ← NEW — implementation

```

> ⚠️ **Controllers already exist** (created by frontend developer as routing-only stubs).
> Your job in Phase 5 is to **UPDATE** the existing controllers by injecting service dependencies.
> Do NOT create new controller files.

---

## 5. Phase-by-Phase Implementation

---

### Phase 1 — Entity Classes (Models)

> **Why first?** Every other layer (repositories, services, controllers) depends on these classes. Entities define your database structure.

---

#### File Path:
```
src/main/java/com/eventtracker/entity/User.java
```

```java
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
    }

    public enum Role {
        USER, ORGANIZER, ADMIN
    }
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/entity/Event.java
```

```java
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
```

---

#### File Path:
```
src/main/java/com/eventtracker/entity/Registration.java
```

```java
package com.eventtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_date", updatable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.REGISTERED;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
        if (this.status == null) this.status = Status.REGISTERED;
    }

    public enum Status {
        REGISTERED, CANCELLED, ATTENDED
    }
}
```

---

### Phase 2 — Repository Layer

> **Why second?** Repositories talk to the database. Services use repositories. Build repositories before services.

---

#### File Path:
```
src/main/java/com/eventtracker/repository/UserRepository.java
```

```java
package com.eventtracker.repository;

import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/repository/EventRepository.java
```

```java
package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByStatus(Event.Status status);

    List<Event> findByCategory(String category);

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findAllByOrderByEventDateAsc();
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/repository/RegistrationRepository.java
```

```java
package com.eventtracker.repository;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByUser(User user);

    List<Registration> findByEvent(Event event);

    Optional<Registration> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    long countByEvent(Event event);
}
```

---

### Phase 3 — Service Layer

> **Why third?** Services contain all business logic. Controllers call services — never repositories directly. Always create the interface first, then the implementation.

---

#### File Path:
```
src/main/java/com/eventtracker/service/UserService.java
```

```java
package com.eventtracker.service;

import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User updateUser(User user);
    void deleteUser(Long id);
    List<User> findAllUsers();
    boolean emailExists(String email);
    long countAllUsers();
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/service/impl/UserServiceImpl.java
```

```java
package com.eventtracker.service.impl;

import com.eventtracker.entity.User;
import com.eventtracker.repository.UserRepository;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        // Always encrypt password before saving — NEVER store plain text
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/service/EventService.java
```

```java
package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface EventService {
    Event createEvent(Event event);
    Optional<Event> findById(Long id);
    List<Event> findAllEvents();
    List<Event> findEventsByOrganizer(User organizer);
    List<Event> findEventsByStatus(Event.Status status);
    List<Event> searchEvents(String keyword);
    Event updateEvent(Event event);
    void deleteEvent(Long id);
    long countAllEvents();
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/service/impl/EventServiceImpl.java
```

```java
package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.repository.EventRepository;
import com.eventtracker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findAllEvents() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    @Override
    public List<Event> findEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    @Override
    public List<Event> findEventsByStatus(Event.Status status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public long countAllEvents() {
        return eventRepository.count();
    }
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/service/RegistrationService.java
```

```java
package com.eventtracker.service;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface RegistrationService {
    Registration registerForEvent(User user, Event event);
    Optional<Registration> findByUserAndEvent(User user, Event event);
    List<Registration> findByUser(User user);
    List<Registration> findByEvent(Event event);
    void cancelRegistration(Long registrationId);
    boolean isUserRegistered(User user, Event event);
    long countRegistrations(Event event);
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/service/impl/RegistrationServiceImpl.java
```

```java
package com.eventtracker.service.impl;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.Registration;
import com.eventtracker.entity.User;
import com.eventtracker.repository.RegistrationRepository;
import com.eventtracker.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;

    @Override
    public Registration registerForEvent(User user, Event event) {
        if (isUserRegistered(user, event)) {
            throw new RuntimeException("You are already registered for this event.");
        }
        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .status(Registration.Status.REGISTERED)
                .build();
        return registrationRepository.save(registration);
    }

    @Override
    public Optional<Registration> findByUserAndEvent(User user, Event event) {
        return registrationRepository.findByUserAndEvent(user, event);
    }

    @Override
    public List<Registration> findByUser(User user) {
        return registrationRepository.findByUser(user);
    }

    @Override
    public List<Registration> findByEvent(Event event) {
        return registrationRepository.findByEvent(event);
    }

    @Override
    public void cancelRegistration(Long registrationId) {
        registrationRepository.findById(registrationId).ifPresent(reg -> {
            reg.setStatus(Registration.Status.CANCELLED);
            registrationRepository.save(reg);
        });
    }

    @Override
    public boolean isUserRegistered(User user, Event event) {
        return registrationRepository.existsByUserAndEvent(user, event);
    }

    @Override
    public long countRegistrations(Event event) {
        return registrationRepository.countByEvent(event);
    }
}
```

---

### Phase 4 — Spring Security

> **Why fourth?** Security must exist before you can test any controller. Without it, every page is either open to everyone or blocked entirely. Do this before touching controllers.

---

#### File Path:
```
src/main/java/com/eventtracker/security/CustomUserDetailsService.java
```

```java
package com.eventtracker.security;

import com.eventtracker.entity.User;
import com.eventtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

---

#### File Path:
```
src/main/java/com/eventtracker/config/SecurityConfig.java
```

```java
package com.eventtracker.config;

import com.eventtracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── Public routes: anyone can access ──────────────────────
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/register",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/events"           // Public can browse events list
                ).permitAll()

                // ── Admin-only routes ────────────────────────────────────
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── Organizer routes ─────────────────────────────────────
                .requestMatchers(
                    "/events/create",
                    "/events/*/edit",
                    "/events/*/delete"
                ).hasAnyRole("ORGANIZER", "ADMIN")

                // ── Everything else requires login ───────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")       // POST endpoint Spring Security listens to
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("email")              // ← Must match the input name in login.html
                .passwordParameter("password")           // ← Must match the input name in login.html
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
```

> ⚠️ **Tell the Frontend Developer:** The login HTML form MUST use:
> - `th:action="@{/auth/login}"` and `method="POST"`
> - `<input type="email" name="email" ...>` for the email field
> - `<input type="password" name="password" ...>` for the password field
> - Thymeleaf automatically adds the CSRF token when using `th:action`

---

### Phase 5 — Controller Integration

> **Why fifth?** After services and security are ready, you can now fill in the controller logic. The frontend developer has already created routing-only controller stubs. **You update them — do NOT create new controller files.**

> ⚠️ **Before editing any controller: tell the frontend developer first.** The URL routes (GET mappings) must stay the same. You are only ADDING service injection and method bodies.

---

#### File to UPDATE:
```
src/main/java/com/eventtracker/controller/AuthController.java
```

```java
package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());   // Empty user object for the form
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "This email is already registered.");
            return "auth/register";
        }

        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Account created! Please log in.");
        return "redirect:/auth/login";
    }
}
```

---

#### File to UPDATE:
```
src/main/java/com/eventtracker/controller/DashboardController.java
```

```java
package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.EventService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", currentUser);

        return switch (currentUser.getRole()) {

            case ADMIN -> {
                model.addAttribute("totalUsers", userService.countAllUsers());
                model.addAttribute("totalEvents", eventService.countAllEvents());
                model.addAttribute("allEvents", eventService.findAllEvents());
                model.addAttribute("allUsers", userService.findAllUsers());
                yield "dashboard/dashboard-admin";
            }

            case ORGANIZER -> {
                List<Event> myEvents = eventService.findEventsByOrganizer(currentUser);
                model.addAttribute("myEvents", myEvents);
                model.addAttribute("totalMyEvents", myEvents.size());
                model.addAttribute("upcomingEvents",
                        eventService.findEventsByStatus(Event.Status.UPCOMING));
                yield "dashboard/dashboard-organiser";
            }

            default -> {  // USER
                var registrations = registrationService.findByUser(currentUser);
                model.addAttribute("registrations", registrations);
                model.addAttribute("totalRegistered", registrations.size());
                model.addAttribute("upcomingEvents",
                        eventService.findEventsByStatus(Event.Status.UPCOMING));
                yield "dashboard/dashboard-user";
            }
        };
    }
}
```

---

#### File to UPDATE:
```
src/main/java/com/eventtracker/controller/EventController.java
```

```java
package com.eventtracker.controller;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.service.EventService;
import com.eventtracker.service.RegistrationService;
import com.eventtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;

    // ── List all events (public accessible) ─────────────────────────────────
    @GetMapping
    public String listEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model) {

        var events = (search != null && !search.isBlank())
                ? eventService.searchEvents(search)
                : eventService.findAllEvents();

        model.addAttribute("events", events);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        return "events/event-list";
    }

    // ── Event detail page ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String eventDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        model.addAttribute("event", event);
        model.addAttribute("registrationCount", registrationService.countRegistrations(event));

        // Show registration status if user is logged in
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user ->
                    model.addAttribute("isRegistered",
                            registrationService.isUserRegistered(user, event))
            );
        }

        return "events/event-details";
    }

    // ── Show create event form (organizer/admin only) ────────────────────────
    @GetMapping("/create")
    public String createEventPage(Model model) {
        model.addAttribute("event", new Event());
        return "events/create-event";
    }

    // ── Submit create event ──────────────────────────────────────────────────
    @PostMapping("/create")
    public String createEvent(
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "events/create-event";
        }

        User organizer = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        event.setOrganizer(organizer);
        eventService.createEvent(event);

        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/events";
    }

    // ── Show edit event form ─────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editEventPage(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        model.addAttribute("event", event);
        return "events/edit-event";
    }

    // ── Submit edit event ────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String editEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("event") Event event,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "events/edit-event";
        }

        event.setId(id);
        eventService.updateEvent(event);

        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/events/" + id;
    }

    // ── Delete event ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Event has been deleted.");
        return "redirect:/events";
    }

    // ── Register for an event ────────────────────────────────────────────────
    @PostMapping("/{id}/register")
    public String registerForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            registrationService.registerForEvent(user, event);
            redirectAttributes.addFlashAttribute("successMessage",
                    "You have successfully registered for \"" + event.getTitle() + "\"!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/events/" + id;
    }

    // ── Cancel registration ──────────────────────────────────────────────────
    @PostMapping("/{id}/cancel-registration")
    public String cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        registrationService.findByUserAndEvent(user, event)
                .ifPresent(reg -> registrationService.cancelRegistration(reg.getId()));

        redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled.");
        return "redirect:/events/" + id;
    }
}
```

---

#### File to UPDATE:
```
src/main/java/com/eventtracker/controller/ProfileController.java
```

```java
package com.eventtracker.controller;

import com.eventtracker.entity.User;
import com.eventtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profilePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute User updatedUser,
            RedirectAttributes redirectAttributes) {

        User existing = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update non-sensitive fields — never update email or password here
        existing.setName(updatedUser.getName());
        existing.setPhone(updatedUser.getPhone());

        userService.updateUser(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
}
```

---

### Phase 6 — Data Seeder (Test Data)

> **Why last?** Creates ready-to-use test accounts and events so the entire team can immediately log in and test all three roles without manually inserting database rows.

---

#### File Path:
```
src/main/java/com/eventtracker/config/DataInitializer.java
```

```java
package com.eventtracker.config;

import com.eventtracker.entity.Event;
import com.eventtracker.entity.User;
import com.eventtracker.repository.EventRepository;
import com.eventtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Skip if data already exists — prevents duplicate seed on restart
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initialization.");
            return;
        }

        log.info("========================================");
        log.info("  Seeding database with test data...");
        log.info("========================================");

        // ── Create test users ──────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@eventtracker.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .phone("0123456789")
                .build());

        User organizer = userRepository.save(User.builder()
                .name("Ahmad Organizer")
                .email("organizer@eventtracker.com")
                .password(passwordEncoder.encode("org123"))
                .role(User.Role.ORGANIZER)
                .phone("0198765432")
                .build());

        userRepository.save(User.builder()
                .name("Siti Participant")
                .email("user@eventtracker.com")
                .password(passwordEncoder.encode("user123"))
                .role(User.Role.USER)
                .phone("0112223334")
                .build());

        // ── Create sample events ───────────────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Tech Conference Malaysia 2024")
                .description("Annual technology conference showcasing the latest in AI, cloud computing, and software engineering. Join 200+ industry experts for a full day of talks, workshops, and networking.")
                .location("Kuala Lumpur Convention Centre, KL")
                .eventDate(LocalDate.of(2024, 8, 15))
                .eventTime(LocalTime.of(9, 0))
                .capacity(200)
                .category("Technology")
                .status(Event.Status.UPCOMING)
                .organizer(organizer)
                .price(0.0)
                .build());

        eventRepository.save(Event.builder()
                .title("Business Networking Night")
                .description("Monthly professional networking event for entrepreneurs, managers, and business leaders across Selangor and KL. Light refreshments provided.")
                .location("Empire Hotel, Subang Jaya")
                .eventDate(LocalDate.of(2024, 7, 20))
                .eventTime(LocalTime.of(18, 30))
                .capacity(80)
                .category("Business")
                .status(Event.Status.UPCOMING)
                .organizer(organizer)
                .price(50.0)
                .build());

        eventRepository.save(Event.builder()
                .title("Photography Workshop for Beginners")
                .description("Hands-on photography workshop covering composition, natural lighting, and basic photo editing. Bring your own camera or smartphone.")
                .location("KLCC, Kuala Lumpur")
                .eventDate(LocalDate.of(2024, 7, 28))
                .eventTime(LocalTime.of(10, 0))
                .capacity(30)
                .category("Arts & Culture")
                .status(Event.Status.UPCOMING)
                .organizer(organizer)
                .price(80.0)
                .build());

        eventRepository.save(Event.builder()
                .title("Charity Fun Run 5KM")
                .description("5KM fun run raising funds for underprivileged children's education across Peninsular Malaysia. Registration includes a t-shirt and finisher medal. All fitness levels welcome!")
                .location("Padang Merbok, Kuala Lumpur")
                .eventDate(LocalDate.of(2024, 9, 1))
                .eventTime(LocalTime.of(7, 0))
                .capacity(500)
                .category("Sports & Fitness")
                .status(Event.Status.UPCOMING)
                .organizer(admin)
                .price(30.0)
                .build());

        eventRepository.save(Event.builder()
                .title("Startup Pitch Night")
                .description("An evening where early-stage startups pitch their ideas to a panel of investors. Great opportunity for networking, feedback, and potential funding.")
                .location("MaGIC Cyberjaya")
                .eventDate(LocalDate.of(2024, 8, 5))
                .eventTime(LocalTime.of(19, 0))
                .capacity(150)
                .category("Business")
                .status(Event.Status.UPCOMING)
                .organizer(organizer)
                .price(0.0)
                .build());

        log.info("========================================");
        log.info("  Database seeded successfully!");
        log.info("");
        log.info("  Test Accounts:");
        log.info("  Admin    : admin@eventtracker.com / admin123");
        log.info("  Organizer: organizer@eventtracker.com / org123");
        log.info("  User     : user@eventtracker.com / user123");
        log.info("========================================");
    }
}
```

> After running the app the first time, open your browser at `http://localhost:8080/auth/login` and log in with any of the test accounts above.

---

## 6. Frontend Integration Points (Model Attributes)

This table shows every model attribute the frontend Thymeleaf templates expect. Your controllers must pass attributes with **exactly these names**. If you rename them, the HTML breaks.

| Template | Model Attribute Name | Java Type | Set In |
|---|---|---|---|
| `auth/register.html` | `user` | `User` | `AuthController.registerPage()` |
| `auth/login.html` | *(none needed)* | — | Spring Security handles it |
| `events/event-list.html` | `events` | `List<Event>` | `EventController.listEvents()` |
| `events/event-list.html` | `search` | `String` | `EventController.listEvents()` |
| `events/event-details.html` | `event` | `Event` | `EventController.eventDetails()` |
| `events/event-details.html` | `isRegistered` | `Boolean` | `EventController.eventDetails()` |
| `events/event-details.html` | `registrationCount` | `Long` | `EventController.eventDetails()` |
| `events/create-event.html` | `event` | `Event` | `EventController.createEventPage()` |
| `events/edit-event.html` | `event` | `Event` | `EventController.editEventPage()` |
| `dashboard/dashboard-user.html` | `user` | `User` | `DashboardController.dashboard()` |
| `dashboard/dashboard-user.html` | `registrations` | `List<Registration>` | `DashboardController.dashboard()` |
| `dashboard/dashboard-user.html` | `totalRegistered` | `int` | `DashboardController.dashboard()` |
| `dashboard/dashboard-user.html` | `upcomingEvents` | `List<Event>` | `DashboardController.dashboard()` |
| `dashboard/dashboard-organiser.html` | `user` | `User` | `DashboardController.dashboard()` |
| `dashboard/dashboard-organiser.html` | `myEvents` | `List<Event>` | `DashboardController.dashboard()` |
| `dashboard/dashboard-organiser.html` | `totalMyEvents` | `int` | `DashboardController.dashboard()` |
| `dashboard/dashboard-admin.html` | `user` | `User` | `DashboardController.dashboard()` |
| `dashboard/dashboard-admin.html` | `totalUsers` | `long` | `DashboardController.dashboard()` |
| `dashboard/dashboard-admin.html` | `totalEvents` | `long` | `DashboardController.dashboard()` |
| `dashboard/dashboard-admin.html` | `allUsers` | `List<User>` | `DashboardController.dashboard()` |
| `dashboard/dashboard-admin.html` | `allEvents` | `List<Event>` | `DashboardController.dashboard()` |
| `profile/profile.html` | `user` | `User` | `ProfileController.profilePage()` |

> ⚠️ **NEVER rename a model attribute without telling the frontend developer first.** They are used as `th:text="${event.title}"`, `th:each="reg : ${registrations}"`, etc. in the HTML.

---

## 7. Completion Checklist

Work through this in order. Do NOT skip phases. Check off each item as you finish.

### 🔧 Setup
- [ ] Fixed all broken test dependencies in `pom.xml`
- [ ] Ran `mvn clean install` — BUILD SUCCESS confirmed
- [ ] Created MySQL database: `CREATE DATABASE event_tracker_db;`
- [ ] Created `backend-db` branch in Git

### 🗄️ Phase 1 — Entities
- [ ] `User.java` created in `entity/`
- [ ] `Event.java` created in `entity/`
- [ ] `Registration.java` created in `entity/`
- [ ] Application starts — Hibernate auto-created tables (check MySQL)

### 📦 Phase 2 — Repositories
- [ ] `UserRepository.java` created
- [ ] `EventRepository.java` created
- [ ] `RegistrationRepository.java` created

### ⚙️ Phase 3 — Services
- [ ] `UserService.java` interface created
- [ ] `UserServiceImpl.java` created
- [ ] `EventService.java` interface created
- [ ] `EventServiceImpl.java` created
- [ ] `RegistrationService.java` interface created
- [ ] `RegistrationServiceImpl.java` created

### 🔐 Phase 4 — Security
- [ ] `CustomUserDetailsService.java` created
- [ ] `SecurityConfig.java` created
- [ ] Application starts without security errors

### 🎮 Phase 5 — Controllers Updated
- [ ] `AuthController.java` updated with service injection
- [ ] `DashboardController.java` updated with service injection
- [ ] `EventController.java` updated with service injection
- [ ] `ProfileController.java` updated with service injection

### 🌱 Phase 6 — Data Seeder
- [ ] `DataInitializer.java` created
- [ ] Application started — see seed logs in console
- [ ] Login works: `admin@eventtracker.com / admin123`
- [ ] Login works: `organizer@eventtracker.com / org123`
- [ ] Login works: `user@eventtracker.com / user123`

### 🔗 Integration Testing
- [ ] Events list page shows all 5 seeded events
- [ ] Event details page loads correctly
- [ ] Organizer can create a new event
- [ ] User can register for an event
- [ ] User cannot register twice for the same event
- [ ] Admin dashboard shows correct stats
- [ ] Organizer dashboard shows their events
- [ ] User dashboard shows their registrations
- [ ] Logout redirects to login page

### 📢 Communication
- [ ] Notified frontend developer that backend is ready
- [ ] Confirmed model attribute names match Section 6
- [ ] Committed and pushed to `backend-db` branch
- [ ] Created Pull Request to merge into `main`

---

## 8. Team Rules & Communication

1. **Never modify HTML/CSS/JS files.** Those are frontend territory. Any template changes must go through the frontend developer.

2. **Never change existing URL routes.** The `@GetMapping` routes in controllers are set by the frontend developer. You only add service logic inside the method body.

3. **Always use BCrypt.** `PasswordEncoder` in `SecurityConfig` handles hashing automatically via `UserServiceImpl.registerUser()`. Never store plain passwords.

4. **Use `@RequiredArgsConstructor`.** Always inject dependencies via constructor (Lombok's `@RequiredArgsConstructor` + `private final`), not `@Autowired`.

5. **Communicate before changing model attributes.** If you rename `"events"` to `"eventList"`, every `th:each="event : ${events}"` in the HTML breaks silently.

6. **Never disable Spring Security for testing.** Use the seeded test accounts from Phase 6.

7. **Handle errors gracefully.** Use `try-catch` in controllers where exceptions are expected (e.g., duplicate registration).

8. **Commit to `backend-db` branch only.** Never commit directly to `main` or `frontend-ui`.

9. **Merge strategy:** When both teams are done, do: `backend-db → main`, then `frontend-ui → main`. The frontend developer will handle merging and resolving controller conflicts.

10. **Test on local MySQL.** Credentials are in `application.properties` (host: `localhost:3306`, db: `event_tracker_db`, user: `root`, pass: `root123`).

---

> 📌 **Questions about templates?** → Ask the Frontend Developer
> 📌 **Questions about database schema?** → Refer to Section 3
> 📌 **Questions about service logic?** → Refer to Phase 3
> 📌 **Questions about security?** → Refer to Phase 4
