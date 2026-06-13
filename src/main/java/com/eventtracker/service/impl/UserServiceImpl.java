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

    // ── Organiser-approval workflow ──────────────────────────────────────────

    @Override
    public List<User> findPendingOrganisers() {
        return userRepository.findByRoleAndStatus(User.Role.ORGANIZER, User.Status.PENDING);
    }

    @Override
    public long countPendingOrganisers() {
        return userRepository.countByRoleAndStatus(User.Role.ORGANIZER, User.Status.PENDING);
    }

    @Override
    public void approveOrganiser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(User.Status.APPROVED);
            userRepository.save(user);
        });
    }

    @Override
    public void rejectOrganiser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(User.Status.REJECTED);
            userRepository.save(user);
        });
    }

    @Override
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        // Verify the current password against the stored BCrypt hash
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}