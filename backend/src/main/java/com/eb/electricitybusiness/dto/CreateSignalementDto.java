package com.eb.electricitybusiness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSignalementDto {
    
    @NotBlank(message = "La description est obligatoire")
    private String description;
    
    @NotNull(message = "L'ID de la borne est obligatoire")
    private Long chargingStationId;
    
    private Long reservationId;
}
