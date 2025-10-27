package com.bharath.incometer.entities.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequestDTO(@NotNull String name, @NotNull @Email String email, @NotNull String phoneNumber,
                             @NotNull String password) {
}
