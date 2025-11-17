package com.electriccharge.app.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationDto {
    private Long id;

    @NotNull(message = "L'identifiant utilisateur est obligatoire")
    private Long utilisateurId;

    @NotNull(message = "L'identifiant de la borne est obligatoire")
    private Long chargingStationId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    private String etat; // ACTIVE, TERMINEE, ANNULEE
    private BigDecimal prixALaMinute;
    private BigDecimal totalPrice;
    private String receiptPath;
    
    // Nested objects for frontend consumption
    private BorneDto borne;
    private UtilisateurSimpleDto utilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getChargingStationId() {
        return chargingStationId;
    }

    public void setChargingStationId(Long chargingStationId) {
        this.chargingStationId = chargingStationId;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public BigDecimal getPrixALaMinute() {
        return prixALaMinute;
    }

    public void setPrixALaMinute(BigDecimal prixALaMinute) {
        this.prixALaMinute = prixALaMinute;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BorneDto getBorne() {
        return borne;
    }

    public void setBorne(BorneDto borne) {
        this.borne = borne;
    }

    public UtilisateurSimpleDto getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurSimpleDto utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getReceiptPath() {
        return receiptPath;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }
} 