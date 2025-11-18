package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.security.AuthenticationFacade;
import com.electriccharge.app.service.PdfReceiptService;
import com.electriccharge.app.service.ReservationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final PdfReceiptService pdfReceiptService;
    private final AuthenticationFacade authenticationFacade;

    public ReservationController(
            ReservationService reservationService, 
            PdfReceiptService pdfReceiptService,
            AuthenticationFacade authenticationFacade) {
        this.reservationService = reservationService;
        this.pdfReceiptService = pdfReceiptService;
        this.authenticationFacade = authenticationFacade;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody ReservationDto dto) {
        try {
            ReservationDto saved = reservationService.create(dto);
            return new ResponseEntity<>(ApiResponse.success("Réservation créée", saved), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // IMPORTANT: Ce mapping doit être AVANT /{id} pour éviter que "receipt" soit traité comme un ID
    @GetMapping("/{id}/receipt")
    public ResponseEntity<?> getReceipt(@PathVariable Long id) {
        try {
            ReservationDto reservation = reservationService.getById(id);
            
            // Vérifier si un reçu existe
            if (reservation.getReceiptPath() == null || reservation.getReceiptPath().isEmpty()) {
                return new ResponseEntity<>(
                    ApiResponse.error("Aucun reçu disponible pour cette réservation"), 
                    HttpStatus.NOT_FOUND
                );
            }
            
            // Récupérer le contenu du PDF
            byte[] pdfContent = pdfReceiptService.getReceiptContent(reservation.getReceiptPath());
            
            // Créer la réponse avec le PDF
            ByteArrayResource resource = new ByteArrayResource(pdfContent);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"recu_reservation_" + id + ".pdf\"")
                    .body(resource);
                    
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération du reçu pour la réservation {}: {}", id, ex.getMessage());
            return new ResponseEntity<>(
                ApiResponse.error("Erreur lors de la récupération du reçu"), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(reservationService.getById(id)));
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<ApiResponse<?>> getByUser(@PathVariable Long userId) {
        List<ReservationDto> list = reservationService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/borne/{stationId}")
    public ResponseEntity<ApiResponse<?>> getByStation(@PathVariable Long stationId) {
        List<ReservationDto> list = reservationService.getByChargingStation(stationId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<ApiResponse<?>> getByOwner(@PathVariable Long proprietaireId) {
        if (proprietaireId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("L'ID du propriétaire est requis"));
        }
        List<ReservationDto> list = reservationService.getByOwner(proprietaireId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        List<ReservationDto> list = reservationService.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancel(
            @PathVariable Long id, 
            @RequestParam(required = false) Long requesterId) {
        try {
            logger.debug("Demande d'annulation de la réservation {} avec requesterId: {}", id, requesterId);
            
            // Si requesterId n'est pas fourni, essayer de l'obtenir depuis le contexte d'authentification
            Long actualRequesterId = requesterId;
            if (actualRequesterId == null) {
                actualRequesterId = authenticationFacade.getPrincipal()
                        .filter(principal -> principal instanceof Utilisateur)
                        .map(principal -> ((Utilisateur) principal).getIdUtilisateur())
                        .orElse(null);
                
                if (actualRequesterId != null) {
                    logger.debug("RequesterId extrait du contexte d'authentification: {}", actualRequesterId);
                }
            }
            
            // Si toujours null, retourner une erreur
            if (actualRequesterId == null) {
                logger.warn("Impossible d'identifier l'utilisateur pour l'annulation de la réservation {}", id);
                return new ResponseEntity<>(
                    ApiResponse.error("Impossible d'identifier l'utilisateur. Veuillez vous reconnecter."), 
                    HttpStatus.UNAUTHORIZED
                );
            }
            
            logger.info("Annulation de la réservation {} par l'utilisateur {}", id, actualRequesterId);
            ReservationDto dto = reservationService.cancel(id, actualRequesterId);
            return ResponseEntity.ok(ApiResponse.success("Réservation annulée", dto));
        } catch (Exception ex) {
            logger.error("Erreur lors de l'annulation de la réservation {}: {}", id, ex.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<?>> complete(@PathVariable Long id) {
        try {
            ReservationDto dto = reservationService.complete(id);
            return ResponseEntity.ok(ApiResponse.success("Réservation terminée", dto));
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}/accepter")
    public ResponseEntity<ApiResponse<?>> accepter(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        try {
            Long proprietaireId = body != null && body.get("proprietaireId") != null 
                ? ((Number) body.get("proprietaireId")).longValue() 
                : null;
            
            if (proprietaireId == null) {
                return new ResponseEntity<>(
                    ApiResponse.error("L'ID du propriétaire est requis"), 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            ReservationDto dto = reservationService.accepter(id, proprietaireId);
            return ResponseEntity.ok(ApiResponse.success("Réservation acceptée avec succès. Un reçu PDF a été généré.", dto));
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            logger.error("Erreur lors de l'acceptation de la réservation {}: {}", id, ex.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}/refuser")
    public ResponseEntity<ApiResponse<?>> refuser(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        try {
            Long proprietaireId = body != null && body.get("proprietaireId") != null 
                ? ((Number) body.get("proprietaireId")).longValue() 
                : null;
            String motif = body != null ? (String) body.get("motif") : null;
            
            if (proprietaireId == null) {
                return new ResponseEntity<>(
                    ApiResponse.error("L'ID du propriétaire est requis"), 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            ReservationDto dto = reservationService.refuser(id, proprietaireId, motif);
            return ResponseEntity.ok(ApiResponse.success("Réservation refusée", dto));
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            logger.error("Erreur lors du refus de la réservation {}: {}", id, ex.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
} 