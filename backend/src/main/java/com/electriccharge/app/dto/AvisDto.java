package com.electriccharge.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisDto {
    private Long idAvis;
    private Integer note;
    private String commentaire;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations sur l'utilisateur
    private Long utilisateurId;
    private String utilisateurPseudo;
    private String utilisateurNom;
    private String utilisateurPrenom;
    
    // Informations sur la borne
    private Long chargingStationId;
    private String chargingStationNom;
}
