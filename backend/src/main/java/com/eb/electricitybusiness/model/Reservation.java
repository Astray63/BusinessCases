package com.eb.electricitybusiness.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero_reservation")
    private Long numeroReservation;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;
    
    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;
    
    @Column(name = "prix_a_la_minute", nullable = false, precision = 10, scale = 4)
    private BigDecimal prixALaMinute;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private EtatReservation etat = EtatReservation.EN_ATTENTE;
    
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "receipt_path")
    private String receiptPath;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_station_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChargingStation chargingStation;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Signalement> signalements;
    
    public enum EtatReservation {
        EN_ATTENTE("en_attente"),
        CONFIRMEE("confirmee"),
        ACTIVE("active"),
        TERMINEE("terminee"),
        ANNULEE("annulee"),
        REFUSEE("refusee");
        
        private final String value;
        
        EtatReservation(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
