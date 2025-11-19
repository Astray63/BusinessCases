package com.electriccharge.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "charging_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_borne")
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
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "borne_medias",
        joinColumns = @JoinColumn(name = "borne_id")
    )
    @Column(name = "media_url")
    private List<String> medias = new ArrayList<>();
    
    @Column(name = "instruction_sur_pied")
    private String instructionSurPied;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private Etat etat = Etat.DISPONIBLE;
    
    @Column(name = "occupee")
    private Boolean occupee = false;
    
    @Column(name = "prix_a_la_minute", nullable = false, precision = 10, scale = 4)
    private BigDecimal prixALaMinute;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;
    
    @Column(name = "connector_type", nullable = false, length = 50)
    private String connectorType = "2S";
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    @JsonIgnore
    private Point geom;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "power_output")
    private Double powerOutput;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private Utilisateur owner;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Reservation> reservations = new HashSet<>();
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Avis> avis = new HashSet<>();
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Signalement> signalements = new HashSet<>();
    
    @OneToOne(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private ChargingStationLieu chargingStationLieu;
    
    @Column(name = "address", nullable = false)
    private String address;
    
    /**
     * Force le type de connecteur à "2S" avant toute persistance ou mise à jour
     */
    @PrePersist
    @PreUpdate
    private void enforceConnectorType() {
        this.connectorType = "2S";
    }
    
    public enum Etat {
        DISPONIBLE,
        OCCUPEE,
        EN_PANNE,
        EN_MAINTENANCE
    }
}
