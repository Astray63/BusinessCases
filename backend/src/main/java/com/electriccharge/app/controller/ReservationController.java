package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.service.ReservationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                logger.debug("Authentication: {}, Principal: {}", 
                    authentication != null ? authentication.getName() : "null",
                    authentication != null ? authentication.getPrincipal().getClass().getSimpleName() : "null");
                    
                if (authentication != null && authentication.getPrincipal() instanceof Utilisateur) {
                    Utilisateur user = (Utilisateur) authentication.getPrincipal();
                    actualRequesterId = user.getIdUtilisateur();
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
} 