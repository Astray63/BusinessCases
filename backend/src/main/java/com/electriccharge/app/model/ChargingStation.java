package com.electriccharge.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    
    @Column(name = "name", nullable = false, length = 100)
    private String nom;

    @Column(name = "localisation", nullable = false)
    private String localisation;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "puissance", nullable = false)
    private Integer puissance;
    
    @Column(name = "medias")
    @ElementCollection
    @CollectionTable(name = "borne_medias", joinColumns = @JoinColumn(name = "borne_id"))
    private List<String> medias;
    
    @Column(name = "instruction_sur_pied")
    private String instructionSurPied;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private Etat etat = Etat.DISPONIBLE;
    
    @Column(name = "occupee")
    private Boolean occupee = false;
    
    @Column(name = "prix_a_la_minute", nullable = false, precision = 10, scale = 4)
    private BigDecimal prixALaMinute;
    
    @Column(name = "connector_type", nullable = false, length = 50)
    private String connectorType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Utilisateur owner;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reservation> reservations;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Avis> avis;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Signalement> signalements;
    
    @OneToOne(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChargingStationLieu chargingStationLieu;
    
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "hourly_rate", nullable = false)
    private BigDecimal hourlyRate;
    
    @Column(name = "power_output", nullable = false)
    private Integer powerOutput;

    // Getters and Setters for powerOutput
    public Integer getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(Integer powerOutput) {
        this.powerOutput = powerOutput;
    }

    public enum Etat {
        DISPONIBLE("disponible"),
        OCCUPEE("occupee"),
        HORS_SERVICE("hors_service"),
        MAINTENANCE("maintenance");
        
        private final String value;
        
        Etat(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}