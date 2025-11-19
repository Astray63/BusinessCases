package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.AvisDto;
import com.electriccharge.app.dto.CreateAvisDto;
import com.electriccharge.app.service.AvisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AvisController {
    
    private final AvisService avisService;
    
    /**
     * Récupère tous les avis pour une borne
     */
    @GetMapping("/borne/{chargingStationId}")
    public ResponseEntity<ApiResponse<List<AvisDto>>> getAvisByChargingStation(
            @PathVariable Long chargingStationId) {
        try {
            List<AvisDto> avis = avisService.getAvisByChargingStation(chargingStationId);
            return ResponseEntity.ok(ApiResponse.success("Avis récupérés avec succès", avis));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des avis pour la borne {}", chargingStationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des avis: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère la note moyenne d'une borne
     */
    @GetMapping("/borne/{chargingStationId}/moyenne")
    public ResponseEntity<ApiResponse<Double>> getAverageNote(@PathVariable Long chargingStationId) {
        try {
            Double moyenne = avisService.getAverageNoteByChargingStation(chargingStationId);
            return ResponseEntity.ok(ApiResponse.success("Note moyenne récupérée avec succès", moyenne));
        } catch (Exception e) {
            log.error("Erreur lors du calcul de la note moyenne pour la borne {}", chargingStationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du calcul de la note moyenne: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère tous les avis d'un utilisateur connecté
     */
    @GetMapping("/mes-avis")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<List<AvisDto>>> getMyAvis() {
        try {
            // L'utilisateur sera récupéré depuis le contexte de sécurité dans le service
            List<AvisDto> avis = avisService.getAvisByUser(null);
            return ResponseEntity.ok(ApiResponse.success("Vos avis récupérés avec succès", avis));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des avis de l'utilisateur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération de vos avis: " + e.getMessage()));
        }
    }
    
    /**
     * Crée un nouvel avis
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<AvisDto>> createAvis(@Valid @RequestBody CreateAvisDto createAvisDto) {
        try {
            AvisDto avis = avisService.createAvis(createAvisDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Avis créé avec succès", avis));
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de l'avis", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'avis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la création de l'avis: " + e.getMessage()));
        }
    }
    
    /**
     * Supprime un avis
     */
    @DeleteMapping("/{avisId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponse<Void>> deleteAvis(@PathVariable Long avisId) {
        try {
            avisService.deleteAvis(avisId);
            return ResponseEntity.ok(ApiResponse.success("Avis supprimé avec succès"));
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'avis {}", avisId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'avis {}", avisId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la suppression de l'avis: " + e.getMessage()));
        }
    }
}
