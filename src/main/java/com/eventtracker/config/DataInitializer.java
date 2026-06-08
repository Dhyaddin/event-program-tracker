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