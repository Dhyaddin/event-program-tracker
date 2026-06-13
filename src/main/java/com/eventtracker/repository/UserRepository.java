package com.eventtracker.repository;

import com.eventtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Organiser-approval workflow
    List<User> findByRoleAndStatus(User.Role role, User.Status status);

    long countByRoleAndStatus(User.Role role, User.Status status);
}