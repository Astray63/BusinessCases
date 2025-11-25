package com.eb.electricitybusiness.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    @Column(name = "code_postal", nullable = false, length = 20)
    private String codePostal;

    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @Column(name = "pays", nullable = false, length = 100)
    private String pays = "France";

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    @JsonIgnore
    private Point geom;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "lieu")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UtilisateurLieu> utilisateurLieux;

    @OneToMany(mappedBy = "lieu")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ChargingStationLieu> chargingStationLieux;
}
