package com.softuni.finalexam.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDto {

    @NotBlank(message = "{order.fullName.notBlank}")
    @Size(min = 2, max = 100, message = "{order.fullName.size}")
    private String fullName;

    @NotBlank(message = "{order.address.notBlank}")
    @Size(min = 5, max = 200, message = "{order.address.size}")
    private String address;

    @NotBlank(message = "{order.phoneNumber.notBlank}")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "{order.phoneNumber.pattern}")
    private String phoneNumber;

    @NotBlank(message = "{order.courier.notBlank}")
    private String courier;

    @NotBlank(message = "{order.paymentMethod.notBlank}")
    private String paymentMethod;
}

