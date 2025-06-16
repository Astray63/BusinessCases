package com.electriccharge.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {
    
    @Id
    @Column(name = "plaque_immatriculation", length = 20)
    private String plaqueImmatriculation;
    
    @Column(name = "marque", nullable = false, length = 50)
    private String marque;
    
    @Column(name = "modele", nullable = false, length = 50)
    private String modele;
    
    @Column(name = "annee")
    private Integer annee;
    
    @Column(name = "capacite_batterie")
    private Integer capaciteBatterie;
    
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
}