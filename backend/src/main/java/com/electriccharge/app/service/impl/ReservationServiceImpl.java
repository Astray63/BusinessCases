package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.exception.DuplicateResourceException;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Reservation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            ChargingStationRepository chargingStationRepository,
            UtilisateurRepository utilisateurRepository) {
        this.reservationRepository = reservationRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public ReservationDto create(ReservationDto dto) {
        logger.debug("Creating reservation for user {} on station {}", dto.getUtilisateurId(), dto.getChargingStationId());

        validateDates(dto);

        ChargingStation station = chargingStationRepository.findById(dto.getChargingStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Borne", "id", dto.getChargingStationId()));
        Utilisateur utilisateur = utilisateurRepository.findById(dto.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", dto.getUtilisateurId()));

        // Check conflicting reservations
        var conflicts = reservationRepository.findConflictingReservations(
                station.getIdBorne(), dto.getDateDebut(), dto.getDateFin());
        if (!conflicts.isEmpty()) {
            throw new DuplicateResourceException("Conflit de réservation : la plage horaire est déjà réservée");
        }

        Reservation reservation = new Reservation();
        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setChargingStation(station);
        reservation.setUtilisateur(utilisateur);
        reservation.setPrixALaMinute(station.getPrixALaMinute());

        long minutes = ChronoUnit.MINUTES.between(dto.getDateDebut(), dto.getDateFin());
        BigDecimal totalPrice = station.getPrixALaMinute().multiply(BigDecimal.valueOf(minutes)).setScale(2, RoundingMode.HALF_UP);
        reservation.setTotalPrice(totalPrice);

        Reservation saved = reservationRepository.save(reservation);
        return convertToDto(saved);
    }

    @Override
    public ReservationDto cancel(Long reservationId, Long requesterId) {
        logger.debug("Cancelling reservation {} by requester {}", reservationId, requesterId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (!reservation.getUtilisateur().getIdUtilisateur().equals(requesterId)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à annuler cette réservation");
        }
        reservation.setEtat(Reservation.EtatReservation.ANNULEE);
        Reservation saved = reservationRepository.save(reservation);
        return convertToDto(saved);
    }

    @Override
    public ReservationDto complete(Long reservationId) {
        logger.debug("Completing reservation {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        reservation.setEtat(Reservation.EtatReservation.TERMINEE);
        Reservation saved = reservationRepository.save(reservation);
        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDto getById(Long id) {
        return convertToDto(reservationRepository.findWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getByUser(Long userId) {
        return reservationRepository.findByUtilisateur_IdUtilisateur(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getByChargingStation(Long stationId) {
        return reservationRepository.findByChargingStation_IdBorne(stationId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getAll() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateDates(ReservationDto dto) {
        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires");
        }
        if (!dto.getDateDebut().isBefore(dto.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }
    }

    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getNumeroReservation());
        dto.setUtilisateurId(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getIdUtilisateur() : null);
        dto.setChargingStationId(reservation.getChargingStation() != null ? reservation.getChargingStation().getIdBorne() : null);
        dto.setDateDebut(reservation.getDateDebut());
        dto.setDateFin(reservation.getDateFin());
        dto.setEtat(reservation.getEtat() != null ? reservation.getEtat().name() : null);
        dto.setPrixALaMinute(reservation.getPrixALaMinute());
        dto.setTotalPrice(reservation.getTotalPrice());
        return dto;
    }
} 