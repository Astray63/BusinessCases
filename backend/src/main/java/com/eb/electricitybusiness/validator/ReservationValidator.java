package com.eb.electricitybusiness.validator;

import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReservationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ReservationValidator.class);
    private final ReservationRepository reservationRepository;

    public ReservationValidator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Valide les dates dans un DTO de réservation
     */
    public ValidationResult validateDates(ReservationDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getDateDebut() == null) {
            errors.add("La date de début est obligatoire");
        }

        if (dto.getDateFin() == null) {
            errors.add("La date de fin est obligatoire");
        }

        if (dto.getDateDebut() != null && dto.getDateFin() != null) {
            if (!dto.getDateDebut().isBefore(dto.getDateFin())) {
                errors.add("La date de début doit être antérieure à la date de fin");
            }

            if (dto.getDateDebut().isBefore(LocalDateTime.now())) {
                errors.add("La date de début ne peut pas être dans le passé");
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Valide qu'il n'y a pas de réservations conflictuelles pour la station et la
     * plage horaire données
     */
    public ValidationResult validateNoConflicts(Long stationId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        return validateNoConflicts(stationId, dateDebut, dateFin, null);
    }

    /**
     * Valide qu'il n'y a pas de réservations conflictuelles pour la station et la
     * plage horaire données,
     * en excluant une réservation spécifique (utile pour les mises à jour)
     */
    public ValidationResult validateNoConflicts(Long stationId, LocalDateTime dateDebut, LocalDateTime dateFin,
            Long excludeReservationId) {
        var conflicts = reservationRepository.findConflictingReservations(stationId, dateDebut, dateFin);

        // Filtrer la réservation en cours de modification si applicable
        if (excludeReservationId != null) {
            conflicts = conflicts.stream()
                    .filter(r -> !r.getNumeroReservation().equals(excludeReservationId))
                    .toList();
        }

        if (!conflicts.isEmpty()) {
            logger.debug("Found {} conflicting reservations for station {} between {} and {}",
                    conflicts.size(), stationId, dateDebut, dateFin);
            return ValidationResult.failure("Conflit de réservation : la plage horaire est déjà réservée");
        }

        return ValidationResult.success();
    }

    /**
     * Valide qu'une borne de recharge est disponible pour la réservation
     */
    public ValidationResult validateStationAvailability(Borne borne) {
        List<String> errors = new ArrayList<>();

        if (borne == null) {
            errors.add("La borne de recharge n'existe pas");
            return ValidationResult.failure(errors);
        }

        if (borne.getEtat() == Borne.Etat.EN_PANNE) {
            errors.add("La borne est hors service");
        }

        if (borne.getEtat() == Borne.Etat.EN_MAINTENANCE) {
            errors.add("La borne est en maintenance");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Valide qu'un utilisateur est autorisé à effectuer une action sur une
     * réservation
     */
    public ValidationResult validateUserAuthorization(Reservation reservation, Long userId, String action) {
        if (reservation.getUtilisateur() == null) {
            return ValidationResult.failure("La réservation n'a pas d'utilisateur associé");
        }

        if (!reservation.getUtilisateur().getIdUtilisateur().equals(userId)) {
            return ValidationResult.failure("Vous n'êtes pas autorisé à " + action + " cette réservation");
        }

        return ValidationResult.success();
    }

    /**
     * Valide qu'un propriétaire est autorisé à effectuer une action sur une
     * réservation
     */
    public ValidationResult validateOwnerAuthorization(Reservation reservation, Long ownerId, String action) {
        if (reservation.getBorne() == null || reservation.getBorne().getOwner() == null) {
            return ValidationResult.failure("La réservation n'a pas de propriétaire de borne associé");
        }

        if (!reservation.getBorne().getOwner().getIdUtilisateur().equals(ownerId)) {
            return ValidationResult.failure("Vous n'êtes pas autorisé à " + action + " cette réservation");
        }

        return ValidationResult.success();
    }

    /**
     * Valide qu'une réservation peut être acceptée
     */
    public ValidationResult validateCanBeAccepted(Reservation reservation) {
        if (reservation.getEtat() != Reservation.EtatReservation.EN_ATTENTE) {
            return ValidationResult.failure("Seules les réservations en attente peuvent être acceptées");
        }
        return ValidationResult.success();
    }

    /**
     * Valide qu'une réservation peut être refusée
     */
    public ValidationResult validateCanBeRefused(Reservation reservation) {
        if (reservation.getEtat() != Reservation.EtatReservation.EN_ATTENTE) {
            return ValidationResult.failure("Seules les réservations en attente peuvent être refusées");
        }
        return ValidationResult.success();
    }

    /**
     * Valide qu'une réservation peut être annulée
     */
    public ValidationResult validateCanBeCancelled(Reservation reservation) {
        if (reservation.getEtat() == Reservation.EtatReservation.TERMINEE) {
            return ValidationResult.failure("Une réservation terminée ne peut pas être annulée");
        }
        if (reservation.getEtat() == Reservation.EtatReservation.ANNULEE) {
            return ValidationResult.failure("Cette réservation est déjà annulée");
        }
        return ValidationResult.success();
    }

    /**
     * Valide une demande de création de réservation complète
     */
    public ValidationResult validateReservationCreation(ReservationDto dto, Borne borne) {
        List<String> allErrors = new ArrayList<>();

        // Valider les dates
        ValidationResult dateValidation = validateDates(dto);
        if (!dateValidation.isValid()) {
            allErrors.addAll(dateValidation.getErrors());
        }

        // Valider la disponibilité de la borne
        ValidationResult stationValidation = validateStationAvailability(borne);
        if (!stationValidation.isValid()) {
            allErrors.addAll(stationValidation.getErrors());
        }

        // Valider l'absence de conflits (seulement si les dates sont valides)
        if (dateValidation.isValid() && dto.getBorneId() != null) {
            ValidationResult conflictValidation = validateNoConflicts(
                    dto.getBorneId(),
                    dto.getDateDebut(),
                    dto.getDateFin());
            if (!conflictValidation.isValid()) {
                allErrors.addAll(conflictValidation.getErrors());
            }
        }

        return allErrors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(allErrors);
    }
}
