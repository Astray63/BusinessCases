package com.eb.electricitybusiness.dto;
import jakarta.validation.Valid;



public class RegisterRequestDto {
    @Valid
    private UtilisateurDto utilisateur;
    private String motDePasse;
    
    // getters et setters
    public UtilisateurDto getUtilisateur() {
        return utilisateur;
    }
    public void setUtilisateur(UtilisateurDto utilisateur) {
        this.utilisateur = utilisateur;
    }
    public String getMotDePasse() {
        return motDePasse;
    }
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
