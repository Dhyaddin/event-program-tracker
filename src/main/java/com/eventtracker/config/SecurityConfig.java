package com.eventtracker.config;

import com.eventtracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

    // Private — NOT a @Bean. Avoids double-registration with global AuthenticationManager.
    private DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(this.userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
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
                        .requestMatchers("/dashboard/admin").hasRole("ADMIN")

                        // ── Organizer-only routes ────────────────────────────────
                        .requestMatchers("/organiser/**").hasRole("ORGANIZER")
                        .requestMatchers("/dashboard/organiser").hasRole("ORGANIZER")
                        .requestMatchers(
                                "/events/create",
                                "/events/*/edit",
                                "/events/*/delete"
                        ).hasAnyRole("ORGANIZER", "ADMIN")

                        // ── User-only routes ──────────────────────────────────────
                        .requestMatchers("/dashboard/user").hasRole("USER")
                        .requestMatchers("/user/**").hasRole("USER")

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