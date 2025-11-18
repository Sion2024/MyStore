package com.softuni.finalexam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

    @Mock
    private RestTemplate restTemplate;

    private NotificationClient notificationClient;

    private final String notificationServiceUrl = "http://localhost:8081/api/v1/notifications";

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(restTemplate, notificationServiceUrl);
    }

    @Test
    void testSendWelcomeEmail_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String firstName = "John";

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        notificationClient.sendWelcomeEmail(userId, firstName);

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq(notificationServiceUrl + "/emails/welcome"),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void testSendWelcomeEmail_Failure() {
        // Given
        UUID userId = UUID.randomUUID();
        String firstName = "John";

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // When
        notificationClient.sendWelcomeEmail(userId, firstName);

        // Then - should not throw exception, just log error
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Void.class));
    }

    @Test
    void testSendOrderConfirmationEmail_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String fullName = "John Doe";
        String address = "123 Main St";
        String phoneNumber = "1234567890";
        String courier = "DHL";
        String paymentMethod = "Credit Card";

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        notificationClient.sendOrderConfirmationEmail(userId, fullName, address, phoneNumber, courier, paymentMethod);

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq(notificationServiceUrl + "/emails/order/confirmation"),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void testSendNewOrderEmail_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String fullName = "John Doe";
        String address = "123 Main St";
        String phoneNumber = "1234567890";
        String courier = "DHL";
        String paymentMethod = "Credit Card";

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        notificationClient.sendNewOrderEmail(userId, fullName, address, phoneNumber, courier, paymentMethod);

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq(notificationServiceUrl + "/emails/order/new"),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void testSendOrderShippedEmail_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("99.99");
        String paymentMethod = "Credit Card";
        String courier = "DHL";
        String address = "123 Main St";

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        notificationClient.sendOrderShippedEmail(userId, orderId, totalAmount, paymentMethod, courier, address);

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq(notificationServiceUrl + "/emails/order/shipped"),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void testUpsertNotificationPreference_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        boolean newsletterEnabled = true;

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        notificationClient.upsertNotificationPreference(userId, email, newsletterEnabled);

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq(notificationServiceUrl + "/preferences"),
                any(),
                eq(Void.class)
        );
    }
}

