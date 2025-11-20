package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, String> {
    
    List<Vehicule> findByUser_IdUtilisateur(Long idUtilisateur);
    
    List<Vehicule> findByMarque(String marque);
    
    List<Vehicule> findByModele(String modele);
} 