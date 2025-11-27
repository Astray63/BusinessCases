package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    /**
     * Récupère tous les avis pour une borne spécifique
     */
    List<Avis> findByBorneIdBorneOrderByCreatedAtDesc(Long chargingStationId);

    /**
     * Récupère tous les avis d'un utilisateur
     */
    List<Avis> findByUtilisateurIdUtilisateurOrderByCreatedAtDesc(Long utilisateurId);

    /**
     * Vérifie si un utilisateur a déjà laissé un avis sur une borne
     */
    boolean existsByUtilisateurIdUtilisateurAndBorneIdBorne(Long utilisateurId, Long chargingStationId);

    /**
     * Calcule la note moyenne d'une borne
     */
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.borne.idBorne = :chargingStationId")
    Double getAverageNoteByBorne(@Param("chargingStationId") Long chargingStationId);

    /**
     * Compte le nombre d'avis pour une borne
     */
    long countByBorneIdBorne(Long chargingStationId);
}
