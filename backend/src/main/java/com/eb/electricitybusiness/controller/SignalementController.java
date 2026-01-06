package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.CreateSignalementDto;
import com.eb.electricitybusiness.dto.SignalementDto;
import com.eb.electricitybusiness.model.Signalement.StatutSignalement;
import com.eb.electricitybusiness.service.SignalementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signalements")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class SignalementController {

    private final SignalementService signalementService;

    /**
     * Récupère tous les signalements pour une borne
     */
    @GetMapping("/borne/{chargingStationId}")
    public ResponseEntity<ApiResponse<List<SignalementDto>>> getSignalementsByChargingStation(
            @PathVariable Long chargingStationId) {
        try {
            List<SignalementDto> signalements = signalementService
                    .getSignalementsByChargingStation(chargingStationId);
            return ResponseEntity.ok(ApiResponse.success("Signalements récupérés avec succès", signalements));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des signalements pour la borne {}", chargingStationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des signalements: " + e.getMessage()));
        }
    }

    /**
     * Récupère tous les signalements d'un utilisateur connecté
     */
    @GetMapping("/mes-signalements")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<List<SignalementDto>>> getMySignalements() {
        try {
            List<SignalementDto> signalements = signalementService.getSignalementsByUser(null);
            return ResponseEntity.ok(ApiResponse.success("Vos signalements récupérés avec succès", signalements));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des signalements de l'utilisateur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération de vos signalements: " + e.getMessage()));
        }
    }

    /**
     * Récupère les signalements par statut
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<List<SignalementDto>>> getSignalementsByStatut(
            @PathVariable String statut) {
        try {
            StatutSignalement statutEnum = parseStatutSignalement(statut);
            if (statutEnum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Statut invalide: " + statut +
                                ". Statuts valides: OUVERT, EN_COURS, RESOLU, FERME"));
            }
            List<SignalementDto> signalements = signalementService.getSignalementsByStatut(statutEnum);
            return ResponseEntity.ok(ApiResponse.success("Signalements récupérés avec succès", signalements));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des signalements par statut {}", statut, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des signalements: " + e.getMessage()));
        }
    }

    /**
     * Compte le nombre de signalements ouverts pour une borne
     */
    @GetMapping("/borne/{chargingStationId}/count")
    public ResponseEntity<ApiResponse<Long>> countOpenSignalements(@PathVariable Long chargingStationId) {
        try {
            long count = signalementService.countOpenSignalements(chargingStationId);
            return ResponseEntity.ok(ApiResponse.success("Nombre de signalements récupéré avec succès", count));
        } catch (Exception e) {
            log.error("Erreur lors du comptage des signalements pour la borne {}", chargingStationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du comptage des signalements: " + e.getMessage()));
        }
    }

    /**
     * Crée un nouveau signalement
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<SignalementDto>> createSignalement(
            @Valid @RequestBody CreateSignalementDto createSignalementDto) {
        try {
            SignalementDto signalement = signalementService.createSignalement(createSignalementDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Signalement créé avec succès", signalement));
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du signalement", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la création du signalement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la création du signalement: " + e.getMessage()));
        }
    }

    /**
     * Met à jour le statut d'un signalement (propriétaire uniquement)
     */
    @PatchMapping("/{signalementId}/statut")
    @PreAuthorize("hasRole('PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<SignalementDto>> updateStatut(
            @PathVariable Long signalementId,
            @RequestParam String statut) {
        try {
            StatutSignalement statutEnum = parseStatutSignalement(statut);
            if (statutEnum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Statut invalide: " + statut +
                                ". Statuts valides: OUVERT, EN_COURS, RESOLU, FERME"));
            }
            SignalementDto signalement = signalementService.updateStatut(signalementId, statutEnum);
            return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", signalement));
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du statut du signalement {}", signalementId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du statut du signalement {}", signalementId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la mise à jour du statut: " + e.getMessage()));
        }
    }

    /**
     * Parse le statut de signalement de manière tolérante
     */
    private StatutSignalement parseStatutSignalement(String statutStr) {
        if (statutStr == null || statutStr.isEmpty()) {
            return null;
        }

        String normalized = statutStr.toUpperCase().trim().replace(" ", "_");

        switch (normalized) {
            case "OUVERT":
            case "OPEN":
            case "NOUVEAU":
                return StatutSignalement.OUVERT;
            case "EN_COURS":
            case "ENCOURS":
            case "IN_PROGRESS":
            case "PENDING":
                return StatutSignalement.EN_COURS;
            case "RESOLU":
            case "RÉSOLU":
            case "RESOLVED":
            case "FIXED":
                return StatutSignalement.RESOLU;
            case "FERME":
            case "FERMÉ":
            case "CLOSED":
                return StatutSignalement.FERME;
            default:
                try {
                    return StatutSignalement.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }

    /**
     * Supprime un signalement
     */
    @DeleteMapping("/{signalementId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<Void>> deleteSignalement(@PathVariable Long signalementId) {
        try {
            signalementService.deleteSignalement(signalementId);
            return ResponseEntity.ok(ApiResponse.success("Signalement supprimé avec succès"));
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du signalement {}", signalementId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du signalement {}", signalementId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la suppression du signalement: " + e.getMessage()));
        }
    }
}
