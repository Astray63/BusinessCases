package com.electriccharge.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chargingStationsProprietaire", "vehicules", "reservations", "avis", "signalements", "utilisateurLieux"})
public class Utilisateur implements UserDetails {
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return pseudo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !estBanni;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;
    
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;
    
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;
    
    @Column(name = "pseudo", unique = true, length = 50)
    private String pseudo;
    
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.client;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "adresse_physique")
    private String adressePhysique;
    
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "medias")
    @ElementCollection
    @CollectionTable(name = "utilisateur_medias", joinColumns = @JoinColumn(name = "utilisateur_id"))
    private List<String> medias;
    
    @Column(name = "est_banni")
    private Boolean estBanni = false;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "verification_code", length = 64)
    private String verificationCode;
    
    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "iban")
    private String iban;
    
    // Relations
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChargingStation> chargingStationsProprietaire = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Vehicule> vehicules = new HashSet<>();
    
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();
    
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Avis> avis = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Signalement> signalements = new HashSet<>();
    
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UtilisateurLieu> utilisateurLieux = new HashSet<>();
    
    public enum Role {
        client("client"),
        proprietaire("proprietaire"),
        admin("admin");
        
        private final String value;
        
        Role(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
