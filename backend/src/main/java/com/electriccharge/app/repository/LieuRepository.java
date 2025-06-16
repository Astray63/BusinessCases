package com.electriccharge.app.repository;

import com.electriccharge.app.model.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LieuRepository extends JpaRepository<Lieu, Long> {
    
    List<Lieu> findByAdresse(String adresse);
    
    List<Lieu> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT l FROM Lieu l JOIN FETCH l.adresse WHERE l.idLieu = :id")
    Optional<Lieu> findWithAdresse(Long id);
} 