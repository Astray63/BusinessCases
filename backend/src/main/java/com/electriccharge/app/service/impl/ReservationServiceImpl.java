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
import com.electriccharge.app.service.PdfReceiptService;
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
    private final PdfReceiptService pdfReceiptService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            ChargingStationRepository chargingStationRepository,
            UtilisateurRepository utilisateurRepository,
            PdfReceiptService pdfReceiptService) {
        this.reservationRepository = reservationRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.pdfReceiptService = pdfReceiptService;
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
    public ReservationDto accepter(Long reservationId, Long proprietaireId) {
        logger.debug("Accepting reservation {} by owner {}", reservationId, proprietaireId);
        
        Reservation reservation = reservationRepository.findWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la borne
        if (reservation.getChargingStation() == null || 
            reservation.getChargingStation().getOwner() == null ||
            !reservation.getChargingStation().getOwner().getIdUtilisateur().equals(proprietaireId)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à accepter cette réservation");
        }
        
        // Vérifier que la réservation est en attente
        if (reservation.getEtat() != Reservation.EtatReservation.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les réservations en attente peuvent être acceptées");
        }
        
        // Changer le statut
        reservation.setEtat(Reservation.EtatReservation.CONFIRMEE);
        
        // Générer le reçu PDF
        try {
            String receiptPath = pdfReceiptService.generateReceipt(reservation);
            reservation.setReceiptPath(receiptPath);
            logger.info("Reçu PDF généré pour la réservation #{}: {}", reservationId, receiptPath);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du reçu PDF pour la réservation #{}", reservationId, e);
            // On continue même si la génération du PDF échoue
        }
        
        Reservation saved = reservationRepository.save(reservation);
        return convertToDto(saved);
    }

    @Override
    public ReservationDto refuser(Long reservationId, Long proprietaireId, String motif) {
        logger.debug("Refusing reservation {} by owner {} with reason: {}", reservationId, proprietaireId, motif);
        
        Reservation reservation = reservationRepository.findWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la borne
        if (reservation.getChargingStation() == null || 
            reservation.getChargingStation().getOwner() == null ||
            !reservation.getChargingStation().getOwner().getIdUtilisateur().equals(proprietaireId)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à refuser cette réservation");
        }
        
        // Vérifier que la réservation est en attente
        if (reservation.getEtat() != Reservation.EtatReservation.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les réservations en attente peuvent être refusées");
        }
        
        // Changer le statut
        reservation.setEtat(Reservation.EtatReservation.REFUSEE);
        
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
    public List<ReservationDto> getByOwner(Long ownerId) {
        return reservationRepository.findByChargingStation_Owner_IdUtilisateur(ownerId).stream()
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
        dto.setReceiptPath(reservation.getReceiptPath());
        
        // Populate nested objects
        if (reservation.getChargingStation() != null) {
            dto.setBorne(convertBorneToDto(reservation.getChargingStation()));
        }
        
        if (reservation.getUtilisateur() != null) {
            dto.setUtilisateur(convertUtilisateurToSimpleDto(reservation.getUtilisateur()));
        }
        
        return dto;
    }
    
    private com.electriccharge.app.dto.BorneDto convertBorneToDto(ChargingStation station) {
        com.electriccharge.app.dto.BorneDto dto = new com.electriccharge.app.dto.BorneDto();
        dto.setId(station.getIdBorne());
        dto.setNumero(station.getNumero());
        dto.setNom(station.getNom());
        dto.setLocalisation(station.getLocalisation());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setPrixALaMinute(station.getPrixALaMinute());
        dto.setPuissance(station.getPuissance());
        dto.setInstructionSurPied(station.getInstructionSurPied());
        dto.setConnectorType(station.getConnectorType());
        dto.setDescription(station.getDescription());
        dto.setEtat(station.getEtat() != null ? station.getEtat().name() : null);
        dto.setOccupee(station.getOccupee());
        
        try {
            if (station.getOwner() != null) {
                dto.setOwnerId(station.getOwner().getIdUtilisateur());
            }
        } catch (Exception e) {
            logger.warn("Could not load owner for station {}", station.getIdBorne());
        }
        
        return dto;
    }
    
    private com.electriccharge.app.dto.UtilisateurSimpleDto convertUtilisateurToSimpleDto(Utilisateur utilisateur) {
        com.electriccharge.app.dto.UtilisateurSimpleDto dto = new com.electriccharge.app.dto.UtilisateurSimpleDto();
        dto.setIdUtilisateur(utilisateur.getIdUtilisateur());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setPseudo(utilisateur.getPseudo());
        dto.setEmail(utilisateur.getEmail());
        dto.setDateNaissance(utilisateur.getDateNaissance());
        dto.setRole(utilisateur.getRole() != null ? utilisateur.getRole().name() : null);
        dto.setIban(utilisateur.getIban());
        dto.setAdressePhysique(utilisateur.getAdressePhysique());
        
        // Convert List<String> to String (comma-separated or first element)
        if (utilisateur.getMedias() != null && !utilisateur.getMedias().isEmpty()) {
            dto.setMedias(String.join(",", utilisateur.getMedias()));
        }
        
        dto.setAccountLocked(!utilisateur.isAccountNonLocked());
        return dto;
    }
} 