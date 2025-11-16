package com.electriccharge.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    
    // Statistiques Client
    private ClientStats clientStats;
    
    // Statistiques Propriétaire (null si l'utilisateur n'est pas propriétaire)
    private OwnerStats ownerStats;
    
    // Réservations récentes
    private List<ReservationDto> recentReservations;
    
    // Bornes récentes (pour les propriétaires)
    private List<BorneDto> recentBornes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientStats {
        private int totalReservations;
        private int reservationsEnCours;
        private int reservationsConfirmees;
        private int reservationsTerminees;
        private int reservationsAnnulees;
        private BigDecimal montantTotalDepense;
        private BigDecimal montantMoisEnCours;
        private ReservationDto prochaineReservation;
        private int reservationsCeMois;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerStats {
        private int totalBornes;
        private int bornesDisponibles;
        private int bornesOccupees;
        private int bornesMaintenance;
        private int bornesHorsService;
        private int demandesEnAttente;
        private int reservationsConfirmees;
        private BigDecimal revenusEstimesMois;
        private BigDecimal revenusTotaux;
        private int totalReservations;
        private BorneDto borneLaPlusReservee;
        private double tauxOccupationMoyen;
    }
}
