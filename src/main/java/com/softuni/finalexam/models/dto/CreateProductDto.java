package com.softuni.finalexam.models.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto {

    @NotBlank(message = "{product.name.notBlank}")
    @Size(min = 2, max = 100, message = "{product.name.size}")
    private String name;

    @NotBlank(message = "{product.description.notBlank}")
    @Size(min = 10, max = 1000, message = "{product.description.size}")
    private String description;

    @NotNull(message = "{product.price.notNull}")
    @DecimalMin(value = "0.01", message = "{product.price.positive}")
    private BigDecimal price;

    @NotNull(message = "{product.stock.notNull}")
    @Min(value = 0, message = "{product.stock.positive}")
    private Integer stock;

    private UUID categoryId;

    @Size(max = 1000, message = "{product.imageUrl.size}")
    private String imageUrl;
}

