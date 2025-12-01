package com.softuni.finalexam.client;

import com.softuni.finalexam.models.dto.notification.NewUserRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationServiceClient {

    @PostMapping("/users/registered")
    ResponseEntity<Void> notifyNewUserRegistration(@RequestBody NewUserRegistrationRequest request);
}

