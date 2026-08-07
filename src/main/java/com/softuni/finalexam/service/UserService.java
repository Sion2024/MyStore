package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.UserRole;
import com.softuni.finalexam.exception.UserAlreadyExistsException;
import com.softuni.finalexam.exception.UserNotFoundException;
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
            throw new UserAlreadyExistsException("Потребител с имейл " + registrationDto.getEmail() + " вече съществува");
        }

        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

        User user = User.builder()
                .name(registrationDto.getFirstName())
                .email(registrationDto.getEmail())
                .password(hashedPassword)
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Send welcome email to the newly registered user
        try {
            notificationClient.notifyNewUserRegistration(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail()
            );
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
                .orElseThrow(() -> new UserNotFoundException("Потребител с ID " + userId + " не е намерен"));

        // Check if new email is the same as current email
        if (user.getEmail() != null && user.getEmail().equals(newEmail)) {
            throw new IllegalArgumentException("Новият имейл е същият като текущия имейл");
        }

        // Check if new email already exists
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new UserAlreadyExistsException("Имейл " + newEmail + " вече се използва");
        }

        // Update email
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);

        log.info("Updated email for user {} to {}", userId, newEmail);
        return updatedUser;
    }
}

