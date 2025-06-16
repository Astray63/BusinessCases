package com.electriccharge.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "lieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lieu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lieu")
    private Long idLieu;
    
    @Column(name = "adresse", nullable = false)
    private String adresse;
    
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    
    @Column(name = "numero", length = 20)
    private String numero;
    
    @Column(name = "rue")
    private String rue;
    
    @Column(name = "code_postal", nullable = false, length = 20)
    private String codePostal;
    
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;
    
    @Column(name = "pays", nullable = false, length = 100)
    private String pays = "France";
    
    @Column(name = "region", length = 100)
    private String region;
    
    @Column(name = "complement_etape")
    private String complementEtape;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @OneToMany(mappedBy = "lieu")
    private Set<UtilisateurLieu> utilisateurLieux;
    
    @OneToMany(mappedBy = "lieu")
    private Set<ChargingStationLieu> chargingStationLieux;
}
