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