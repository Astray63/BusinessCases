package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Signalement;
import com.eb.electricitybusiness.model.Signalement.StatutSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    
    /**
     * Récupère tous les signalements pour une borne spécifique
     */
    List<Signalement> findByChargingStationIdBorneOrderByDateSignalementDesc(Long chargingStationId);
    
    /**
     * Récupère tous les signalements d'un utilisateur
     */
    List<Signalement> findByUserIdUtilisateurOrderByDateSignalementDesc(Long userId);
    
    /**
     * Récupère les signalements par statut
     */
    List<Signalement> findByStatutOrderByDateSignalementDesc(StatutSignalement statut);
    
    /**
     * Récupère les signalements d'une borne par statut
     */
    List<Signalement> findByChargingStationIdBorneAndStatutOrderByDateSignalementDesc(
            Long chargingStationId, StatutSignalement statut);
    
    /**
     * Compte le nombre de signalements ouverts pour une borne
     */
    long countByChargingStationIdBorneAndStatut(Long chargingStationId, StatutSignalement statut);
}
