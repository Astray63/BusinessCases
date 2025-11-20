package com.eb.electricitybusiness.dto;

import com.eb.electricitybusiness.model.Signalement.StatutSignalement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalementDto {
    private Long idSignalement;
    private String description;
    private StatutSignalement statut;
    private LocalDateTime dateSignalement;
    private LocalDateTime dateResolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations sur l'utilisateur
    private Long userId;
    private String userPseudo;
    private String userNom;
    private String userPrenom;
    
    // Informations sur la borne
    private Long chargingStationId;
    private String chargingStationNom;
    
    // Informations sur la réservation (optionnel)
    private Long reservationId;
}
