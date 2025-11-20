package com.eb.electricitybusiness.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UtilisateurDto(
    Long idUtilisateur,
    String role,
    
    @NotBlank(message = "Le nom est obligatoire")
    String nom,
    
    @NotBlank(message = "Le prénom est obligatoire")
    String prenom,
    
    @NotBlank(message = "Le pseudo est obligatoire")
    String pseudo,
    
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    String email,
    
    @Past(message = "La date de naissance doit être dans le passé")
    LocalDate dateNaissance,
    
    String iban,
    String adressePhysique,
    String medias,
    Long idAdresse,
    Boolean actif,
    LocalDateTime dateCreation,
    LocalDateTime dateModification
) {
}