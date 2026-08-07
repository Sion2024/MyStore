package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.UserRole;
import com.softuni.finalexam.exception.UserAlreadyExistsException;
import com.softuni.finalexam.exception.UserNotFoundException;
import com.softuni.finalexam.models.dto.UserRegistrationDto;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationClient notificationClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, notificationClient);
    }

    @Test
    void testRegisterUser_Success() {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name("John")
                .email("john@example.com")
                .password("hashedPassword")
                .role(UserRole.USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.registerUser(registrationDto);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(notificationClient, times(1)).notifyNewUserRegistration(
                eq(savedUser.getId()),
                eq("John"),
                eq("john@example.com")
        );
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        // When/Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(notificationClient, never()).notifyNewUserRegistration(any(), any(), any());
    }

    @Test
    void testUpdateEmail_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String newEmail = "newemail@example.com";
        
        User user = User.builder()
                .id(userId)
                .name("John")
                .email("oldemail@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateEmail(userId, newEmail);

        // Then
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateEmail_UserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        String newEmail = "newemail@example.com";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(userId, newEmail));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateEmail_EmailAlreadyExists() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        String newEmail = "existing@example.com";
        
        User user = User.builder()
                .id(userId)
                .email("oldemail@example.com")
                .build();
        
        User existingUser = User.builder()
                .id(otherUserId)
                .email(newEmail)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(existingUser));

        // When/Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateEmail(userId, newEmail));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateEmail_SameEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        String sameEmail = "same@example.com";
        
        User user = User.builder()
                .id(userId)
                .email(sameEmail)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateEmail(userId, sameEmail));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticate_Success_WithHashedPassword() {
        // Given
        String email = "john@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(hashedPassword)
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        // When
        Optional<User> result = userService.authenticate(email, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, hashedPassword);
    }

    @Test
    void testAuthenticate_Success_WithPlainTextPassword() {
        // Given
        String email = "john@example.com";
        String password = "password123";
        
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(password) // Plain text password
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, password)).thenReturn(false);

        // When
        Optional<User> result = userService.authenticate(email, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        // Given
        String email = "john@example.com";
        String password = "wrongpassword";
        String hashedPassword = "hashedPassword";
        
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(hashedPassword)
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

        // When
        Optional<User> result = userService.authenticate(email, password);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, hashedPassword);
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.authenticate(email, password);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testGetById_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John")
                .email("john@example.com")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.getById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetById_UserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        User result = userService.getById(userId);

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findById(userId);
    }
}

