package com.softuni.finalexam.models.dto.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRegistrationRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String userName;

    @NotBlank
    @Email
    private String userEmail;
}

