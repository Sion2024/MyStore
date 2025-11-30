package com.softuni.finalexam.models.dto.notification;

import com.softuni.finalexam.enums.EmailType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippedEmailRequest {

    @NotBlank
    private String subject;

    @NotNull
    private EmailType emailType;

    @NotNull
    private UUID userId;

    @NotNull
    private Long orderId;

    @NotNull
    @Min(0)
    private BigDecimal totalAmount;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String courier;

    @NotBlank
    private String address;
}

