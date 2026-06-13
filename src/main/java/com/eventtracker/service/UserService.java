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

    // Organiser-approval workflow
    List<User> findPendingOrganisers();
    long countPendingOrganisers();
    void approveOrganiser(Long id);
    void rejectOrganiser(Long id);

    /** Change a user's password. Returns false if the current password does not match. */
    boolean changePassword(String email, String currentPassword, String newPassword);
}