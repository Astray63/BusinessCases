package com.electriccharge.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class BorneDto {
    private Long id;

    @NotBlank(message = "Le numéro est obligatoire")
    private String numero;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;

    @NotNull(message = "La latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    private Double longitude;

    @NotNull(message = "Le prix à la minute est obligatoire")
    @Positive(message = "Le prix à la minute doit être positif")
    private BigDecimal prixALaMinute;

    @NotNull(message = "La puissance est obligatoire")
    @Positive(message = "La puissance doit être positive")
    private Integer puissance;

    private List<String> medias;
    private String instructionSurPied;
    private String connectorType = "2S"; // Valeur fixe, non modifiable
    private String description;

    @NotBlank(message = "L'état est obligatoire")
    private String etat;

    private Boolean occupee;

    @NotNull(message = "L'owner id est obligatoire")
    private Long ownerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getPrixALaMinute() {
        return prixALaMinute;
    }

    public void setPrixALaMinute(BigDecimal prixALaMinute) {
        this.prixALaMinute = prixALaMinute;
    }

    public Integer getPuissance() {
        return puissance;
    }

    public void setPuissance(Integer puissance) {
        this.puissance = puissance;
    }

    public List<String> getMedias() {
        return medias;
    }

    public void setMedias(List<String> medias) {
        this.medias = medias;
    }

    public String getInstructionSurPied() {
        return instructionSurPied;
    }

    public void setInstructionSurPied(String instructionSurPied) {
        this.instructionSurPied = instructionSurPied;
    }

    public String getConnectorType() {
        return "2S"; // Toujours retourner "2S"
    }

    public void setConnectorType(String connectorType) {
        // Ignorer toute valeur passée, toujours forcer à "2S"
        this.connectorType = "2S";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Boolean getOccupee() {
        return occupee;
    }

    public void setOccupee(Boolean occupee) {
        this.occupee = occupee;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}