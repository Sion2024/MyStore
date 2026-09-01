package com.softuni.finalexam.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for NotificationClient
 *
 * NOTE: This test requires the notification service to be running on port 8082
 * To run this test, remove @Disabled and ensure notification service is running
 */
@SpringBootTest
@TestPropertySource(properties = {
        "notification.service.url=http://localhost:8082/api/v1/notifications"
})
@Disabled("Enable this test when notification service is running on port 8082")
class NotificationClientIntegrationTest {

    @Autowired
    private NotificationClient notificationClient;

    @Test
    void testNotifyNewUserRegistration_Integration() {
        // Given
        UUID userId = UUID.randomUUID();
        String userName = "TestUser";
        String userEmail = "test@example.com";

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.notifyNewUserRegistration(userId, userName, userEmail);
        });
    }

    @Test
    void testNotifyNewOrder_Integration() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String customerName = "Test User";
        String customerEmail = "test@example.com";
        LocalDateTime orderDate = LocalDateTime.now();
        BigDecimal totalAmount = new BigDecimal("99.99");

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.notifyNewOrder(orderId, userId, customerName, customerEmail, orderDate, totalAmount);
        });
    }
}
