package com.softuni.finalexam.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private boolean newsletterEnabled = false;
}

