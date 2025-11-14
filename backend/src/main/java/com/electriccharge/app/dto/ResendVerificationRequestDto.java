package com.electriccharge.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequestDto(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    String email
) {}
