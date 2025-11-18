package com.softuni.finalexam.service;

import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationClient = notificationClient;
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Register a new user and send welcome email via notification service
     */
    @Transactional
    public User registerUser(String firstName, String email, String password, String role, boolean newsletterEnabled) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(password);

        // Create and save user
        User user = User.builder()
                .name(firstName)
                .email(email)
                .password(hashedPassword)
                .role(role != null && !role.isEmpty() ? role : "USER")
                .build();

        User savedUser = userRepository.save(user);

        // Create notification preference in notification service
        try {
            notificationClient.upsertNotificationPreference(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    newsletterEnabled
            );
        } catch (Exception e) {
            log.warn("Failed to create notification preference for user: {}", savedUser.getId(), e);
        }

        // Send welcome email via notification service
        try {
            notificationClient.sendWelcomeEmail(savedUser.getId(), firstName);
        } catch (Exception e) {
            log.warn("Failed to send welcome email for user: {}", savedUser.getId(), e);
        }

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Authenticate user for login
     */
    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if password matches (handle both hashed and plain text for migration)
            if (passwordEncoder.matches(password, user.getPassword()) || 
                user.getPassword().equals(password)) { // Fallback for existing plain text passwords
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

