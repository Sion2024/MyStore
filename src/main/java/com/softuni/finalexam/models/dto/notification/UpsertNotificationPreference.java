package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpsertNotificationPreference {

    @NotNull
    private UUID userId;

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private boolean isNewsletterEnabled;

    @NotNull
    @NotBlank
    private String contactData;
}

