package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByEmail(String email);
    
    Optional<Utilisateur> findByPseudo(String pseudo);
    
    boolean existsByEmail(String email);
    
    boolean existsByPseudo(String pseudo);
    
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.vehicules WHERE u.idUtilisateur = :id")
    Optional<Utilisateur> findWithVehicules(Long id);
} 