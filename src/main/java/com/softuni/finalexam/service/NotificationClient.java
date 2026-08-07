package com.softuni.finalexam.service;

import com.softuni.finalexam.client.NotificationServiceClient;
import com.softuni.finalexam.models.dto.notification.NewOrderRequest;
import com.softuni.finalexam.models.dto.notification.NewUserRegistrationRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationClient {

    private final NotificationServiceClient notificationServiceClient;

    /**
     * Notify admin when a new user registers - sends email to admin (tsvetanov777@gmail.com)
     */
    public void notifyNewUserRegistration(UUID userId, String userName, String userEmail) {
        try {
            NewUserRegistrationRequest request = new NewUserRegistrationRequest();
            request.setUserId(userId);
            request.setUserName(userName);
            request.setUserEmail(userEmail);

            notificationServiceClient.notifyUserRegistration(request);
            log.info("Welcome email sent successfully for user: {} ({})", userId, userEmail);
        } catch (FeignException e) {
            log.error("Failed to send welcome email for user: {} ({})", userId, userEmail, e);
        }
    }

    /**
     * Notify admin when a new order is created - sends email to admin (tsvetanov777@gmail.com)
     */
    public void notifyNewOrder(UUID orderId, UUID userId, String customerName, String customerEmail, 
                               LocalDateTime orderDate, BigDecimal totalAmount) {
        try {
            NewOrderRequest request = new NewOrderRequest();
            request.setOrderId(orderId);
            request.setUserId(userId);
            request.setCustomerName(customerName);
            request.setCustomerEmail(customerEmail);
            request.setOrderDate(orderDate);
            request.setTotalAmount(totalAmount);

            notificationServiceClient.notifyNewOrder(request);
            log.info("New order notification sent successfully for order: {} (user: {})", orderId, userId);
        } catch (FeignException e) {
            log.error("Failed to send new order notification for order: {} (user: {})", orderId, userId, e);
        }
    }
}

