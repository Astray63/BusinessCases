package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.mapper.ReservationMapper;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Reservation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.PdfReceiptService;
import com.electriccharge.app.service.PriceCalculator;
import com.electriccharge.app.service.ReservationService;
import com.electriccharge.app.validator.ReservationValidator;
import com.electriccharge.app.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public ReservationDto create(ReservationDto dto) {
        logger.debug("Creating reservation for user {} on station {}", dto.getUtilisateurId(), dto.getChargingStationId());

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
        ValidationResult ownerAuthResult = validator.validateOwnerAuthorization(reservation, proprietaireId, "accepter");
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
    public List<ReservationDto> getAll() {
        return mapper.toDtoList(reservationRepository.findAll());
    }
}