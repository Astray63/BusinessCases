package com.eb.electricitybusiness.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequestDto(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    String email,

    @NotBlank(message = "Le code de vérification est obligatoire")
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir 6 chiffres")
    String code
) {}
