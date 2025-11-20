package com.eb.electricitybusiness.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
    @NotBlank(message = "Le login est obligatoire")
    String pseudo,
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    String password
) {
}
