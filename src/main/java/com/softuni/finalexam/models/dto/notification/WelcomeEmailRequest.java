package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.EmailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WelcomeEmailRequest {

    @NotBlank
    private String subject;

    @NotNull
    private EmailType emailType;

    @NotNull
    private UUID userId;

    @NotBlank
    private String userFirstName;
}

