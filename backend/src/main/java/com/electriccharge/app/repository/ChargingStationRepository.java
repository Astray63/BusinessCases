package com.electriccharge.app.repository;

import com.electriccharge.app.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    
    List<ChargingStation> findByChargingStationLieu_Lieu_IdLieu(Long idLieu);
    
    List<ChargingStation> findByOccupee(Boolean occupee);
    
    List<ChargingStation> findByEtat(String etat);
    
    List<ChargingStation> findByPuissance(Double puissance);
    
    List<ChargingStation> findByLocalisationContainingIgnoreCase(String localisation);
    
    List<ChargingStation> findByPrixALaMinuteBetween(BigDecimal minPrix, BigDecimal maxPrix);
    
    @Query("SELECT b FROM ChargingStation b LEFT JOIN FETCH b.chargingStationLieu bl LEFT JOIN FETCH bl.lieu WHERE b.idBorne = :id")
    Optional<ChargingStation> findWithLieu(Long id);

    @Query("""
        SELECT b FROM ChargingStation b
        WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(b.latitude)) *
        cos(radians(b.longitude) - radians(:longitude)) +
        sin(radians(:latitude)) * sin(radians(b.latitude)))) < :distance
        """)
    List<ChargingStation> findByDistance(Double latitude, Double longitude, Double distance);
}