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
}

