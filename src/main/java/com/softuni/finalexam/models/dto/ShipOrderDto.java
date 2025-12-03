package com.softuni.finalexam.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipOrderDto {

    @NotBlank(message = "{order.courier.notBlank}")
    private String courier;

    @NotBlank(message = "{order.address.notBlank}")
    @Size(min = 5, max = 200, message = "{order.address.size}")
    private String address;

    @NotBlank(message = "{order.paymentMethod.notBlank}")
    private String paymentMethod;
}

