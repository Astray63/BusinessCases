package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LieuRepository extends JpaRepository<Lieu, Long> {
    
    List<Lieu> findByAdresse(String adresse);
    
    List<Lieu> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT l FROM Lieu l JOIN FETCH l.adresse WHERE l.idLieu = :id")
    Optional<Lieu> findWithAdresse(Long id);
    
    @Query(value = 
        "SELECT * FROM lieu l " +
        "WHERE (6371 * acos(" +
        "cos(radians(:latitude)) * cos(radians(l.latitude)) * cos(radians(l.longitude) - radians(:longitude)) + " +
        "sin(radians(:latitude)) * sin(radians(l.latitude))" +
        ")) < :distance", nativeQuery = true)
    List<Lieu> findByDistance(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("distance") Double distance
    );
} 