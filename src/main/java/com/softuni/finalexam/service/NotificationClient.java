package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.EmailType;
import com.softuni.finalexam.enums.NotificationType;
import com.softuni.finalexam.models.dto.notification.OrderCreateEmailRequest;
import com.softuni.finalexam.models.dto.notification.OrderShippedEmailRequest;
import com.softuni.finalexam.models.dto.notification.UpsertNotificationPreference;
import com.softuni.finalexam.models.dto.notification.WelcomeEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.service.url}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    /**
     * Send welcome email when a new user registers
     */
    public void sendWelcomeEmail(UUID userId, String userFirstName) {
        try {
            WelcomeEmailRequest request = new WelcomeEmailRequest();
            request.setSubject("Welcome to MyStore!");
            request.setEmailType(EmailType.WELCOME);
            request.setUserId(userId);
            request.setUserFirstName(userFirstName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WelcomeEmailRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/emails/welcome",
                    entity,
                    Void.class
            );

            log.info("Welcome email sent successfully for user: {}", userId);
        } catch (RestClientException e) {
            log.error("Failed to send welcome email for user: {}", userId, e);
        }
    }

    /**
     * Send order confirmation email when an order is created
     */
    public void sendOrderConfirmationEmail(UUID userId, String fullName, String address,
                                          String phoneNumber, String courier, String paymentMethod) {
        try {
            OrderCreateEmailRequest request = new OrderCreateEmailRequest();
            request.setSubject("Order Confirmation - MyStore");
            request.setEmailType(EmailType.ORDER_CONFIRMATION);
            request.setUserId(userId);
            request.setFullName(fullName);
            request.setAddress(address);
            request.setPhoneNumber(phoneNumber);
            request.setCourier(courier);
            request.setPaymentMethod(paymentMethod);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateEmailRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/emails/order/confirmation",
                    entity,
                    Void.class
            );

            log.info("Order confirmation email sent successfully for user: {}", userId);
        } catch (RestClientException e) {
            log.error("Failed to send order confirmation email for user: {}", userId, e);
        }
    }

    /**
     * Send new order notification email (for sellers)
     */
    public void sendNewOrderEmail(UUID userId, String fullName, String address,
                                  String phoneNumber, String courier, String paymentMethod) {
        try {
            OrderCreateEmailRequest request = new OrderCreateEmailRequest();
            request.setSubject("New Order Received - MyStore");
            request.setEmailType(EmailType.NEW_ORDER);
            request.setUserId(userId);
            request.setFullName(fullName);
            request.setAddress(address);
            request.setPhoneNumber(phoneNumber);
            request.setCourier(courier);
            request.setPaymentMethod(paymentMethod);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateEmailRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/emails/order/new",
                    entity,
                    Void.class
            );

            log.info("New order email sent successfully for user: {}", userId);
        } catch (RestClientException e) {
            log.error("Failed to send new order email for user: {}", userId, e);
        }
    }

    /**
     * Send order shipped email when an order status changes to IN_TRANSIT
     * Note: orderId should be converted from UUID to Long - using most significant bits
     */
    public void sendOrderShippedEmail(UUID userId, UUID orderId, BigDecimal totalAmount,
                                     String paymentMethod, String courier, String address) {
        try {
            OrderShippedEmailRequest request = new OrderShippedEmailRequest();
            request.setSubject("Your Order Has Been Shipped!");
            request.setEmailType(EmailType.SHIPPED_ORDER);
            request.setUserId(userId);
            // Convert UUID to Long using most significant bits
            request.setOrderId(orderId.getMostSignificantBits() & Long.MAX_VALUE);
            request.setTotalAmount(totalAmount);
            request.setPaymentMethod(paymentMethod);
            request.setCourier(courier);
            request.setAddress(address);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderShippedEmailRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/emails/order/shipped",
                    entity,
                    Void.class
            );

            log.info("Order shipped email sent successfully for user: {} and order: {}", userId, orderId);
        } catch (RestClientException e) {
            log.error("Failed to send order shipped email for user: {} and order: {}", userId, orderId, e);
        }
    }

    /**
     * Create or update notification preferences for a user
     */
    public void upsertNotificationPreference(UUID userId, String email, boolean newsletterEnabled) {
        try {
            UpsertNotificationPreference request = new UpsertNotificationPreference();
            request.setUserId(userId);
            request.setNotificationType(NotificationType.EMAIL);
            request.setNewsletterEnabled(newsletterEnabled);
            request.setContactData(email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpsertNotificationPreference> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    notificationServiceUrl + "/preferences",
                    entity,
                    Void.class
            );

            log.info("Notification preference updated successfully for user: {}", userId);
        } catch (RestClientException e) {
            log.error("Failed to update notification preference for user: {}", userId, e);
        }
    }
}

