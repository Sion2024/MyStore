package com.softuni.finalexam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for NotificationClient
 * 
 * NOTE: This test requires the notification service to be running on port 8081
 * To run this test, uncomment @Disabled annotation and ensure notification service is running
 */
@SpringBootTest
@TestPropertySource(properties = {
        "notification.service.url=http://localhost:8081/api/v1/notifications"
})
@Disabled("Enable this test when notification service is running on port 8081")
class NotificationClientIntegrationTest {

    private NotificationClient notificationClient;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplateConfig().restTemplate();
        notificationClient = new NotificationClient(
                restTemplate,
                "http://localhost:8081/api/v1/notifications"
        );
    }

    @Test
    void testSendWelcomeEmail_Integration() {
        // Given
        UUID userId = UUID.randomUUID();
        String firstName = "TestUser";

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.sendWelcomeEmail(userId, firstName);
        });
    }

    @Test
    void testSendOrderConfirmationEmail_Integration() {
        // Given
        UUID userId = UUID.randomUUID();
        String fullName = "Test User";
        String address = "123 Test Street";
        String phoneNumber = "1234567890";
        String courier = "Test Courier";
        String paymentMethod = "Credit Card";

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.sendOrderConfirmationEmail(
                    userId, fullName, address, phoneNumber, courier, paymentMethod
            );
        });
    }

    @Test
    void testSendOrderShippedEmail_Integration() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("99.99");
        String paymentMethod = "Credit Card";
        String courier = "Test Courier";
        String address = "123 Test Street";

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.sendOrderShippedEmail(
                    userId, orderId, totalAmount, paymentMethod, courier, address
            );
        });
    }

    @Test
    void testUpsertNotificationPreference_Integration() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        boolean newsletterEnabled = true;

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            notificationClient.upsertNotificationPreference(userId, email, newsletterEnabled);
        });
    }
}

