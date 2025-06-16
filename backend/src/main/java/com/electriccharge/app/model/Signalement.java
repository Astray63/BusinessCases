package com.electriccharge.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "signalement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signalement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Long idSignalement;
    
    @CreationTimestamp
    @Column(name = "date_signalement")
    private LocalDateTime dateSignalement;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutSignalement statut = StatutSignalement.OUVERT;
    
    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_station_id")
    private ChargingStation chargingStation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    
    public enum StatutSignalement {
        OUVERT("ouvert"),
        EN_COURS("en_cours"),
        RESOLU("resolu"),
        FERME("ferme");
        
        private final String value;
        
        StatutSignalement(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
