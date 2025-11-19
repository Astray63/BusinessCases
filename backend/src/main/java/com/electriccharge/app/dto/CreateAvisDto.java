package com.electriccharge.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAvisDto {
    
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum de 1")
    @Max(value = 5, message = "La note doit être au maximum de 5")
    private Integer note;
    
    private String commentaire;
    
    @NotNull(message = "L'ID de la borne est obligatoire")
    private Long chargingStationId;
}
