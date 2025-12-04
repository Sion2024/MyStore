package com.softuni.finalexam.service;

import com.softuni.finalexam.client.NotificationServiceClient;
import com.softuni.finalexam.models.dto.notification.NewOrderRequest;
import com.softuni.finalexam.models.dto.notification.NewUserRegistrationRequest;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

    @Mock
    private NotificationServiceClient notificationServiceClient;

    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(notificationServiceClient);
    }

    @Test
    void testNotifyNewUserRegistration_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String userName = "John Doe";
        String userEmail = "john@example.com";

        when(notificationServiceClient.notifyUserRegistration(any(NewUserRegistrationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        notificationClient.notifyNewUserRegistration(userId, userName, userEmail);

        // Then
        verify(notificationServiceClient, times(1)).notifyUserRegistration(any(NewUserRegistrationRequest.class));
    }

    @Test
    void testNotifyNewUserRegistration_Failure() {
        // Given
        UUID userId = UUID.randomUUID();
        String userName = "John Doe";
        String userEmail = "john@example.com";

        when(notificationServiceClient.notifyUserRegistration(any(NewUserRegistrationRequest.class)))
                .thenThrow(mock(FeignException.class));

        // When
        notificationClient.notifyNewUserRegistration(userId, userName, userEmail);

        // Then - should not throw exception, just log error
        verify(notificationServiceClient, times(1)).notifyUserRegistration(any(NewUserRegistrationRequest.class));
    }

    @Test
    void testNotifyNewOrder_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String customerName = "John Doe";
        String customerEmail = "john@example.com";
        LocalDateTime orderDate = LocalDateTime.now();
        BigDecimal totalAmount = new BigDecimal("99.99");

        when(notificationServiceClient.notifyNewOrder(any(NewOrderRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        notificationClient.notifyNewOrder(orderId, userId, customerName, customerEmail, orderDate, totalAmount);

        // Then
        verify(notificationServiceClient, times(1)).notifyNewOrder(any(NewOrderRequest.class));
    }

    @Test
    void testNotifyNewOrder_Failure() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String customerName = "John Doe";
        String customerEmail = "john@example.com";
        LocalDateTime orderDate = LocalDateTime.now();
        BigDecimal totalAmount = new BigDecimal("99.99");

        when(notificationServiceClient.notifyNewOrder(any(NewOrderRequest.class)))
                .thenThrow(mock(FeignException.class));

        // When
        notificationClient.notifyNewOrder(orderId, userId, customerName, customerEmail, orderDate, totalAmount);

        // Then - should not throw exception, just log error
        verify(notificationServiceClient, times(1)).notifyNewOrder(any(NewOrderRequest.class));
    }
}

