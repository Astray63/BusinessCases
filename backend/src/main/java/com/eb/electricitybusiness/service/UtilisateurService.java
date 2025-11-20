package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.UtilisateurDto;
import com.eb.electricitybusiness.dto.AuthRequestDto;
import com.eb.electricitybusiness.dto.ChangePasswordRequestDto;
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
    
    void changePassword(Long userId, ChangePasswordRequestDto request);
    
    boolean verifyEmail(String email, String code);
    
    void resendVerificationCode(String email);
} 