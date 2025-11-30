package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.EmailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class WelcomeEmailRequest {

    @NotBlank
    private final String subject;

    @NotNull
    private final EmailType emailType;

    @NotNull
    private final UUID userId;

    @NotBlank
    private final String userFirstName;
}

