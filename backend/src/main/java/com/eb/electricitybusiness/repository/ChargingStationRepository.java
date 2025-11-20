package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    @Query("SELECT cs FROM ChargingStation cs LEFT JOIN FETCH cs.owner WHERE cs.etat = :etat")
    List<ChargingStation> findByEtat(@Param("etat") ChargingStation.Etat etat);
    
    @Query("SELECT cs FROM ChargingStation cs LEFT JOIN FETCH cs.medias WHERE cs.owner.idUtilisateur = :ownerId")
    List<ChargingStation> findByOwner_IdUtilisateur(@Param("ownerId") Long ownerId);
    
    @Query("SELECT cs FROM ChargingStation cs LEFT JOIN FETCH cs.medias WHERE cs.chargingStationLieu.lieu.idLieu = :idLieu")
    List<ChargingStation> findByChargingStationLieu_Lieu_IdLieu(@Param("idLieu") Long idLieu);
    
    @Query(value = 
        "SELECT * FROM charging_stations cs " +
        "WHERE cs.latitude IS NOT NULL AND cs.longitude IS NOT NULL " +
        "AND (6371 * acos(" +
        "cos(radians(:latitude)) * cos(radians(cs.latitude)) * cos(radians(cs.longitude) - radians(:longitude)) + " +
        "sin(radians(:latitude)) * sin(radians(cs.latitude))" +
        ")) < :distance", nativeQuery = true)
    List<ChargingStation> findByDistance(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("distance") Double distance
    );
}