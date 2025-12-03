package com.softuni.finalexam.models.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    @NotBlank(message = "{user.name.notBlank}")
    private String name;

    @Column
    @NotBlank(message = "{user.password.notBlank}")
    private String password;

    @Column
    @NotBlank(message = "{user.email.notBlank}")
    @Email(message = "{user.email.valid}")
    private String email;

    @Column
    private String role;
}
