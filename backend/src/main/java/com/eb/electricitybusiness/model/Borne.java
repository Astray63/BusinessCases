package com.eb.electricitybusiness.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borne")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borne_id")
    private Long idBorne;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "localisation", nullable = false)
    private String localisation;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "puissance", nullable = false)
    private Integer puissance;

    @Column(name = "instruction_sur_pied")
    private String instructionSurPied;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private Etat etat = Etat.DISPONIBLE;

    @Column(name = "occupee")
    private Boolean occupee = false;

    @Column(name = "prix_a_la_minute", nullable = false, precision = 10, scale = 4)
    private BigDecimal prixALaMinute;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    @JsonIgnore
    private Point geom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "motDePasse", "reservations", "bornes", "avis",
            "signalements" })
    private Utilisateur owner;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "borne_medias", joinColumns = @JoinColumn(name = "borne_id"))
    @Column(name = "media_url", columnDefinition = "TEXT")
    private List<String> medias = new ArrayList<>();

    @OneToMany(mappedBy = "borne", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("borne")
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieu_id", nullable = false)
    @JsonIgnoreProperties("bornes")
    private Lieu lieu;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Etat {
        DISPONIBLE,
        OCCUPEE,
        EN_PANNE,
        EN_MAINTENANCE
    }
}
