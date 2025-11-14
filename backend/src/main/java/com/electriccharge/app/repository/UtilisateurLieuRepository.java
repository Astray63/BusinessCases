package com.electriccharge.app.repository;

import com.electriccharge.app.model.Lieu;
import com.electriccharge.app.model.UtilisateurLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurLieuRepository extends JpaRepository<UtilisateurLieu, UtilisateurLieu.UtilisateurLieuId> {
    
    List<UtilisateurLieu> findByUtilisateur_IdUtilisateur(Long userId);
    
    List<UtilisateurLieu> findByLieu_IdLieu(Long lieuId);
    
    @Query("SELECT ul.lieu FROM UtilisateurLieu ul WHERE ul.utilisateur.idUtilisateur = :userId")
    List<Lieu> findLieuxByUtilisateurId(Long userId);
    
    void deleteByUtilisateur_IdUtilisateurAndLieu_IdLieu(Long userId, Long lieuId);
    
    boolean existsByUtilisateur_IdUtilisateurAndLieu_IdLieu(Long userId, Long lieuId);
}
