package com.softuni.finalexam.models.dto.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewOrderRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID userId;

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    @NotNull
    private LocalDateTime orderDate;

    @NotNull
    private BigDecimal totalAmount;
}

