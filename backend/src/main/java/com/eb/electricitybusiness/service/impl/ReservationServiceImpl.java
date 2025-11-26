package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.exception.ResourceNotFoundException;
import com.eb.electricitybusiness.mapper.ReservationMapper;
import com.eb.electricitybusiness.model.ChargingStation;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.ChargingStationRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.PdfReceiptService;
import com.eb.electricitybusiness.service.PriceCalculator;
import com.eb.electricitybusiness.service.ReservationService;
import com.eb.electricitybusiness.validator.ReservationValidator;
import com.eb.electricitybusiness.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PdfReceiptService pdfReceiptService;
    private final ReservationValidator validator;
    private final PriceCalculator priceCalculator;
    private final ReservationMapper mapper;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            ChargingStationRepository chargingStationRepository,
            UtilisateurRepository utilisateurRepository,
            PdfReceiptService pdfReceiptService,
            ReservationValidator validator,
            PriceCalculator priceCalculator,
            ReservationMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.pdfReceiptService = pdfReceiptService;
        this.validator = validator;
        this.priceCalculator = priceCalculator;
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("null")
    public ReservationDto create(ReservationDto dto) {
        logger.debug("Creating reservation for user {} on station {}", dto.getUtilisateurId(),
                dto.getChargingStationId());

        ChargingStation station = chargingStationRepository.findById(dto.getChargingStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Borne", "id", dto.getChargingStationId()));
        Utilisateur utilisateur = utilisateurRepository.findById(dto.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", dto.getUtilisateurId()));

        // Validate using ReservationValidator
        ValidationResult validationResult = validator.validateReservationCreation(dto, station);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getAllErrors());
        }

        Reservation reservation = new Reservation();
        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setChargingStation(station);
        reservation.setUtilisateur(utilisateur);
        reservation.setPrixALaMinute(station.getPrixALaMinute());

        // Calculate price using PriceCalculator
        reservation.setTotalPrice(priceCalculator.calculateTotalPrice(station, dto.getDateDebut(), dto.getDateFin()));

        Reservation saved = reservationRepository.save(reservation);

        // Reload with details to avoid lazy loading issues
        return mapper.toDto(reservationRepository.findWithDetails(saved.getNumeroReservation())
                .orElse(saved));
    }

    @Override
    @SuppressWarnings("null")
    public ReservationDto cancel(Long reservationId, Long requesterId) {
        logger.debug("Cancelling reservation {} by requester {}", reservationId, requesterId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        // Validate using ReservationValidator
        ValidationResult authResult = validator.validateUserAuthorization(reservation, requesterId, "annuler");
        if (!authResult.isValid()) {
            throw new IllegalArgumentException(authResult.getFirstError());
        }

        ValidationResult cancelResult = validator.validateCanBeCancelled(reservation);
        if (!cancelResult.isValid()) {
            throw new IllegalArgumentException(cancelResult.getFirstError());
        }

        reservation.setEtat(Reservation.EtatReservation.ANNULEE);
        Reservation saved = reservationRepository.save(reservation);

        // Reload with details to avoid lazy loading issues
        return mapper.toDto(reservationRepository.findWithDetails(saved.getNumeroReservation())
                .orElse(saved));
    }

    @Override
    @SuppressWarnings("null")
    public ReservationDto complete(Long reservationId) {
        logger.debug("Completing reservation {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        reservation.setEtat(Reservation.EtatReservation.TERMINEE);
        Reservation saved = reservationRepository.save(reservation);

        // Reload with details to avoid lazy loading issues
        return mapper.toDto(reservationRepository.findWithDetails(saved.getNumeroReservation())
                .orElse(saved));
    }

    @Override
    public ReservationDto accepter(Long reservationId, Long proprietaireId) {
        logger.debug("Accepting reservation {} by owner {}", reservationId, proprietaireId);

        Reservation reservation = reservationRepository.findWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        // Validate using ReservationValidator
        ValidationResult ownerAuthResult = validator.validateOwnerAuthorization(reservation, proprietaireId,
                "accepter");
        if (!ownerAuthResult.isValid()) {
            throw new IllegalArgumentException(ownerAuthResult.getFirstError());
        }

        ValidationResult canAcceptResult = validator.validateCanBeAccepted(reservation);
        if (!canAcceptResult.isValid()) {
            throw new IllegalArgumentException(canAcceptResult.getFirstError());
        }

        // Validate no conflicts before accepting
        ValidationResult noConflictResult = validator.validateNoConflicts(
                reservation.getChargingStation().getIdBorne(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservationId);
        if (!noConflictResult.isValid()) {
            throw new IllegalArgumentException(noConflictResult.getFirstError());
        }

        // Change status
        reservation.setEtat(Reservation.EtatReservation.CONFIRMEE);

        // Generate PDF receipt
        try {
            String receiptPath = pdfReceiptService.generateReceipt(reservation);
            reservation.setReceiptPath(receiptPath);
            logger.info("Reçu PDF généré pour la réservation #{}: {}", reservationId, receiptPath);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du reçu PDF pour la réservation #{}", reservationId, e);
            // Continue even if PDF generation fails
        }

        Reservation saved = reservationRepository.save(reservation);

        // Reload with details to avoid lazy loading issues
        return mapper.toDto(reservationRepository.findWithDetails(saved.getNumeroReservation())
                .orElse(saved));
    }

    @Override
    public ReservationDto refuser(Long reservationId, Long proprietaireId, String motif) {
        logger.debug("Refusing reservation {} by owner {} with reason: {}", reservationId, proprietaireId, motif);

        Reservation reservation = reservationRepository.findWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        // Validate using ReservationValidator
        ValidationResult ownerAuthResult = validator.validateOwnerAuthorization(reservation, proprietaireId, "refuser");
        if (!ownerAuthResult.isValid()) {
            throw new IllegalArgumentException(ownerAuthResult.getFirstError());
        }

        ValidationResult canRefuseResult = validator.validateCanBeRefused(reservation);
        if (!canRefuseResult.isValid()) {
            throw new IllegalArgumentException(canRefuseResult.getFirstError());
        }

        // Change status
        reservation.setEtat(Reservation.EtatReservation.REFUSEE);

        Reservation saved = reservationRepository.save(reservation);

        // Reload with details to avoid lazy loading issues
        return mapper.toDto(reservationRepository.findWithDetails(saved.getNumeroReservation())
                .orElse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDto getById(Long id) {
        return mapper.toDto(reservationRepository.findWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getByUser(Long userId) {
        return mapper.toDtoList(reservationRepository.findByUtilisateur_IdUtilisateur(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getByChargingStation(Long stationId) {
        return mapper.toDtoList(reservationRepository.findByChargingStation_IdBorne(stationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getByOwner(Long ownerId) {
        return mapper.toDtoList(reservationRepository.findByChargingStation_Owner_IdUtilisateur(ownerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> filtrer(String statut, java.time.LocalDateTime dateDebut,
            java.time.LocalDateTime dateFin, Long borneId, Long utilisateurId) {
        Specification<Reservation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch relations to avoid N+1
            if (query.getResultType() != Long.class) {
                root.fetch("utilisateur", JoinType.INNER);
                var stationFetch = root.fetch("chargingStation", JoinType.INNER);
                stationFetch.fetch("owner", JoinType.LEFT);
                stationFetch.fetch("medias", JoinType.LEFT);
            }

            if (statut != null && !statut.isEmpty()) {
                try {
                    Reservation.EtatReservation etat = Reservation.EtatReservation.valueOf(statut);
                    predicates.add(cb.equal(root.get("etat"), etat));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }

            if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateDebut"), dateDebut));
            }

            if (dateFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateFin"), dateFin));
            }

            if (borneId != null) {
                predicates.add(cb.equal(root.get("chargingStation").get("idBorne"), borneId));
            }

            if (utilisateurId != null) {
                predicates.add(cb.equal(root.get("utilisateur").get("idUtilisateur"), utilisateurId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return mapper.toDtoList(reservationRepository.findAll(spec));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getAll() {
        return mapper.toDtoList(reservationRepository.findAll());
    }
}