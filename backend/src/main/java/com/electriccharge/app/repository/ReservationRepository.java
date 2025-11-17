package com.electriccharge.app.repository;

import com.electriccharge.app.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.chargingStation WHERE r.utilisateur.idUtilisateur = :idUtilisateur")
    List<Reservation> findByUtilisateur_IdUtilisateur(Long idUtilisateur);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.chargingStation WHERE r.chargingStation.idBorne = :idBorne")
    List<Reservation> findByChargingStation_IdBorne(Long idBorne);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.chargingStation WHERE r.chargingStation.owner.idUtilisateur = :proprietaireId")
    List<Reservation> findByChargingStation_Owner_IdUtilisateur(Long proprietaireId);
    
    List<Reservation> findByEtat(String etat);
    
    @Query("SELECT r FROM Reservation r WHERE r.dateDebut >= :dateDebut AND r.dateFin <= :dateFin")
    List<Reservation> findByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.chargingStation.idBorne = :idBorne 
        AND ((r.dateDebut BETWEEN :dateDebut AND :dateFin) 
        OR (r.dateFin BETWEEN :dateDebut AND :dateFin)
        OR (:dateDebut BETWEEN r.dateDebut AND r.dateFin))
        """)
    List<Reservation> findConflictingReservations(Long idBorne, LocalDateTime dateDebut, LocalDateTime dateFin);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.chargingStation WHERE r.numeroReservation = :id")
    Optional<Reservation> findWithDetails(Long id);
    
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.chargingStation.idBorne = :borneId AND r.etat IN ('EN_COURS', 'CONFIRMEE') AND r.dateFin >= CURRENT_TIMESTAMP")
    boolean hasActiveReservations(Long borneId);
}