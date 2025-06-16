package com.electriccharge.app.service;

import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.dto.AuthRequestDto;
import java.util.List;

public interface UtilisateurService {
    
    UtilisateurDto creerUtilisateur(UtilisateurDto utilisateurDto, String motDePasse);
    
    UtilisateurDto getUtilisateurById(Long id);
    
    List<UtilisateurDto> getAllUtilisateurs();
    
    UtilisateurDto updateUtilisateur(Long id, UtilisateurDto utilisateurDto);
    
    void deleteUtilisateur(Long id);
    
    UtilisateurDto getUtilisateurByEmail(String email);
    
    UtilisateurDto getUtilisateurByPseudo(String pseudo);
    
    List<UtilisateurDto> getUtilisateursWithVehicules();
    
    boolean validerMotDePasse(AuthRequestDto authRequestDto);
    
    void banirUtilisateur(Long id);
    
    void reactiverUtilisateur(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByPseudo(String pseudo);
} 