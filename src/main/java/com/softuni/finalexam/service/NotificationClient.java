package com.softuni.finalexam.service;

import com.softuni.finalexam.client.NotificationServiceClient;
import com.softuni.finalexam.models.dto.notification.NewUserRegistrationRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationClient {

    private final NotificationServiceClient notificationServiceClient;

    public NotificationClient(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    /**
     * Notify admin when a new user registers
     */
    public void notifyNewUserRegistration(UUID userId, String userName, String userEmail) {
        try {
            NewUserRegistrationRequest request = new NewUserRegistrationRequest();
            request.setUserId(userId);
            request.setUserName(userName);
            request.setUserEmail(userEmail);

            notificationServiceClient.notifyNewUserRegistration(request);
            log.info("New user registration notification sent successfully for user: {}", userId);
        } catch (FeignException e) {
            log.error("Failed to send new user registration notification for user: {}", userId, e);
        }
    }
}

