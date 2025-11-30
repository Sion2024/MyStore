package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.EmailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateEmailRequest {

    @NotNull
    private String subject;

    @NotNull
    private EmailType emailType;

    @NotNull
    private UUID userId;

    @NotBlank
    private String fullName;

    @NotBlank
    private String address;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String courier;

    @NotBlank
    private String paymentMethod;
}

