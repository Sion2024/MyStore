package com.softuni.finalexam.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotBlank(message = "{user.email.notBlank}")
    @Email(message = "{user.email.valid}")
    private String email;

    @NotBlank(message = "{user.password.notBlank}")
    private String password;
}

