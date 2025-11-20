package com.eb.electricitybusiness.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "utilisateur_lieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UtilisateurLieu.UtilisateurLieuId.class)
public class UtilisateurLieu {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieu_id")
    private Lieu lieu;
    
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type_adresse")
    private TypeAdresse typeAdresse = TypeAdresse.principale;
    
    public enum TypeAdresse {
        principale,
        secondaire,
        travail
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UtilisateurLieuId implements Serializable {
        private Long utilisateur;
        private Long lieu;
        private TypeAdresse typeAdresse;
    }
}