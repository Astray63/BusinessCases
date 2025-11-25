package com.eb.electricitybusiness.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record LieuDto(
        Long idLieu,

        @NotBlank(message = "Le nom du lieu est obligatoire") @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères") String nom,

        @NotBlank(message = "L'adresse est obligatoire") String adresse,

        @NotBlank(message = "Le code postal est obligatoire") @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères") String codePostal,

        @NotBlank(message = "La ville est obligatoire") @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères") String ville,

        @NotBlank(message = "Le pays est obligatoire") @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères") String pays,

        @DecimalMin(value = "-90.0", message = "La latitude doit être comprise entre -90 et 90") @DecimalMax(value = "90.0", message = "La latitude doit être comprise entre -90 et 90") Double latitude,

        @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180") @DecimalMax(value = "180.0", message = "La longitude doit être comprise entre -180 et 180") Double longitude,

        LocalDateTime createdAt,

        LocalDateTime updatedAt) {
}
