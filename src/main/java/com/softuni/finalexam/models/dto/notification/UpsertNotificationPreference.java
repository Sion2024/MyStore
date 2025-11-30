package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

