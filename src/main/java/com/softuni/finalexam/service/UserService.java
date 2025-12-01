package com.softuni.finalexam.service;

import com.softuni.finalexam.models.dto.UserRegistrationDto;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        Optional<User> existingUser = userRepository.findByEmail(registrationDto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + registrationDto.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

        User user = User.builder()
                .name(registrationDto.getFirstName())
                .email(registrationDto.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);


        try {
            notificationClient.upsertNotificationPreference(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    registrationDto.isNewsletterEnabled()
            );
        } catch (Exception e) {
            log.warn("Failed to create notification preference", e);
        }

        try {
            notificationClient.sendWelcomeEmail(savedUser.getId(), registrationDto.getFirstName());
        } catch (Exception e) {
            log.warn("Failed to send welcome email", e);
        }

        return savedUser;
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // support both hashed and plain text (for migration)
            if (passwordEncoder.matches(password, user.getPassword()) || 
                user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateEmail(UUID userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if new email is the same as current email
        if (user.getEmail() != null && user.getEmail().equals(newEmail)) {
            throw new RuntimeException("New email is the same as current email");
        }

        // Check if new email already exists
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new RuntimeException("Email " + newEmail + " is already in use");
        }

        // Update email
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);

        log.info("Updated email for user {} to {}", userId, newEmail);
        return updatedUser;
    }
}

