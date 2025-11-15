package com.electriccharge.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Vue pour les bornes complètes avec leur localisation
@Entity
@Table(name = "v_borne_complete")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorneComplete {
    
    @Id
    @Column(name = "id_borne")
    private Long idBorne;
    
    @Column(name = "numero")
    private String numero;
    
    @Column(name = "nom")
    private String nom;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "puissance")
    private Integer puissance;
    
    @Column(name = "instruction_sur_pied")
    private String instructionSurPied;
    
    @Column(name = "etat")
    private String etat;
    
    @Column(name = "occupee")
    private Boolean occupee;
    
    @Column(name = "prix_a_la_minute")
    private BigDecimal prixALaMinute;
    
    @Column(name = "connector_type")
    private String connectorType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    @JsonIgnore
    private Point geom;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Informations du lieu
    @Column(name = "adresse")
    private String adresse;
    
    @Column(name = "ville")
    private String ville;
    
    @Column(name = "code_postal")
    private String codePostal;
    
    @Column(name = "pays")
    private String pays;
    
    // Informations du propriétaire
    @Column(name = "proprietaire_nom")
    private String proprietaireNom;
    
    @Column(name = "proprietaire_prenom")
    private String proprietairePrenom;
}

// Vue pour les réservations complètes
@Entity
@Table(name = "v_reservation_complete")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
class ReservationComplete {
    
    @Id
    @Column(name = "numero_reservation")
    private Long numeroReservation;
    
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;
    
    @Column(name = "id_borne")
    private Long idBorne;
    
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;
    
    @Column(name = "date_fin")
    private LocalDateTime dateFin;
    
    @Column(name = "prix_a_la_minute")
    private BigDecimal prixALaMinute;
    
    @Column(name = "etat")
    private String etat;
    
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    
    @Column(name = "receipt_path")
    private String receiptPath;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Informations de l'utilisateur
    @Column(name = "utilisateur_nom")
    private String utilisateurNom;
    
    @Column(name = "utilisateur_prenom")
    private String utilisateurPrenom;
    
    @Column(name = "utilisateur_email")
    private String utilisateurEmail;
    
    // Informations de la borne
    @Column(name = "borne_nom")
    private String borneNom;
    
    @Column(name = "borne_numero")
    private String borneNumero;
    
    // Informations du lieu de la borne
    @Column(name = "borne_adresse")
    private String borneAdresse;
    
    @Column(name = "borne_ville")
    private String borneVille;
}