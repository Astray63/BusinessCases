package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.impl.ReservationServiceImpl;
import com.eb.electricitybusiness.mapper.ReservationMapper;

import com.eb.electricitybusiness.validator.ReservationValidator;
import com.eb.electricitybusiness.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BorneRepository borneRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PdfReceiptService pdfReceiptService;

    @Mock
    private ReservationValidator validator;

    @Mock
    private PriceCalculator priceCalculator;

    @Mock
    private ReservationMapper mapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Utilisateur utilisateur;
    private Borne borne;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(1L);

        borne = new Borne();
        borne.setIdBorne(1L);
        borne.setPrixALaMinute(new BigDecimal("0.50"));

        // Setup default mocks
        when(validator.validateReservationCreation(any(), any())).thenReturn(ValidationResult.success());
        when(priceCalculator.calculateTotalPrice(any(), any(), any())).thenReturn(new BigDecimal("10.00"));
        when(mapper.toDto(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            ReservationDto dto = new ReservationDto();
            dto.setId(r.getNumeroReservation());
            if (r.getUtilisateur() != null)
                dto.setUtilisateurId(r.getUtilisateur().getIdUtilisateur());
            if (r.getBorne() != null)
                dto.setBorneId(r.getBorne().getIdBorne());
            if (r.getEtat() != null)
                dto.setEtat(r.getEtat().name());
            return dto;
        });
        when(mapper.toDtoList(anyList())).thenAnswer(i -> {
            List<Reservation> list = i.getArgument(0);
            return list.stream().map(r -> {
                ReservationDto dto = new ReservationDto();
                dto.setId(r.getNumeroReservation());
                return dto;
            }).collect(java.util.stream.Collectors.toList());
        });
    }

    @Test
    void create_ValidReservation_ReturnsDto() {
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(1L);
        dto.setBorneId(1L);
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        when(borneRepository.findByIdWithLock(1L)).thenReturn(Optional.of(borne));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        // when(reservationRepository.findConflictingReservations(any(), any(),
        // any())).thenReturn(Collections.emptyList()); // Removed as it's handled by
        // validator

        // Fix findWithDetails mock to return the saved reservation
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation saved = i.getArgument(0);
            saved.setNumeroReservation(1L);
            return saved;
        });
        when(reservationRepository.findWithDetails(1L)).thenAnswer(i -> {
            Reservation r = new Reservation();
            r.setNumeroReservation(1L);
            r.setUtilisateur(utilisateur);
            r.setBorne(borne);
            return Optional.of(r);
        });

        ReservationDto result = reservationService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void create_ConflictingReservation_ThrowsException() {
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(1L);
        dto.setBorneId(1L);
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        when(borneRepository.findByIdWithLock(1L)).thenReturn(Optional.of(borne));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // Mock validator to return error
        when(validator.validateReservationCreation(any(), any()))
                .thenReturn(ValidationResult.failure("La borne est déjà réservée pour cette période"));

        assertThrows(IllegalArgumentException.class, () -> reservationService.create(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancel_ValidReservation_ReturnsDto() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setEtat(Reservation.EtatReservation.EN_ATTENTE);
        reservation.setUtilisateur(utilisateur);
        reservation.setBorne(borne);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateUserAuthorization(any(), any(), any())).thenReturn(ValidationResult.success());
        when(validator.validateCanBeCancelled(any())).thenReturn(ValidationResult.success());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));

        ReservationDto result = reservationService.cancel(1L, 1L);

        assertNotNull(result);
        assertEquals(Reservation.EtatReservation.ANNULEE.name(), result.getEtat());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void cancel_ReservationNotFound_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // Use a generic Exception class or specific one if known, based on service impl
        // it throws ResourceNotFoundException
        assertThrows(RuntimeException.class, () -> reservationService.cancel(1L, 1L));
    }

    @Test
    void cancel_UnauthorizedUser_ThrowsException() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateUserAuthorization(any(), any(), any()))
                .thenReturn(ValidationResult.failure("Unauthorized"));

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancel(1L, 2L));
    }

    @Test
    void cancel_InvalidState_ThrowsException() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateUserAuthorization(any(), any(), any())).thenReturn(ValidationResult.success());
        when(validator.validateCanBeCancelled(any()))
                .thenReturn(ValidationResult.failure("Cannot cancel"));

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancel(1L, 1L));
    }

    @Test
    void complete_ValidReservation_ReturnsDto() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setUtilisateur(utilisateur);
        reservation.setBorne(borne);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));

        ReservationDto result = reservationService.complete(1L);

        assertNotNull(result);
        assertEquals(Reservation.EtatReservation.TERMINEE.name(), result.getEtat());
    }

    @Test
    void accepter_ValidReservation_ReturnsDto() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setUtilisateur(utilisateur);
        reservation.setBorne(borne);
        reservation.setDateDebut(LocalDateTime.now());
        reservation.setDateFin(LocalDateTime.now().plusHours(1));

        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateOwnerAuthorization(any(), any(), any())).thenReturn(ValidationResult.success());
        when(validator.validateCanBeAccepted(any())).thenReturn(ValidationResult.success());
        when(validator.validateNoConflicts(any(), any(), any(), any())).thenReturn(ValidationResult.success());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationDto result = reservationService.accepter(1L, 1L);

        assertNotNull(result);
        assertEquals(Reservation.EtatReservation.CONFIRMEE.name(), result.getEtat());
        verify(pdfReceiptService).generateReceipt(any());
    }

    @Test
    void accepter_Conflict_ThrowsException() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setBorne(borne);
        reservation.setDateDebut(LocalDateTime.now());
        reservation.setDateFin(LocalDateTime.now().plusHours(1));

        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateOwnerAuthorization(any(), any(), any())).thenReturn(ValidationResult.success());
        when(validator.validateCanBeAccepted(any())).thenReturn(ValidationResult.success());
        when(validator.validateNoConflicts(any(), any(), any(), any()))
                .thenReturn(ValidationResult.failure("Conflict"));

        assertThrows(IllegalArgumentException.class, () -> reservationService.accepter(1L, 1L));
    }

    @Test
    void refuser_ValidReservation_ReturnsDto() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setUtilisateur(utilisateur);
        reservation.setBorne(borne);

        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));
        when(validator.validateOwnerAuthorization(any(), any(), any())).thenReturn(ValidationResult.success());
        when(validator.validateCanBeRefused(any())).thenReturn(ValidationResult.success());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationDto result = reservationService.refuser(1L, 1L, "Reason");

        assertNotNull(result);
        assertEquals(Reservation.EtatReservation.REFUSEE.name(), result.getEtat());
    }

    @Test
    void getById_Found_ReturnsDto() {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(1L);
        reservation.setUtilisateur(utilisateur);
        reservation.setBorne(borne);

        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.of(reservation));

        ReservationDto result = reservationService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(reservationRepository.findWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> reservationService.getById(1L));
    }

    @Test
    void getByUser_ReturnsList() {
        when(reservationRepository.findByUtilisateur_IdUtilisateur(1L))
                .thenReturn(java.util.List.of(new Reservation()));
        assertFalse(reservationService.getByUser(1L).isEmpty());
    }

    @Test
    void getByChargingStation_ReturnsList() {
        when(reservationRepository.findByBorneIdBorne(1L)).thenReturn(java.util.List.of(new Reservation()));
        assertFalse(reservationService.getByChargingStation(1L).isEmpty());
    }

    @Test
    void getByOwner_ReturnsList() {
        when(reservationRepository.findByBorneOwnerIdUtilisateur(1L)).thenReturn(java.util.List.of(new Reservation()));
        assertFalse(reservationService.getByOwner(1L).isEmpty());
    }

    @Test
    void getAll_ReturnsList() {
        when(reservationRepository.findAll()).thenReturn(java.util.List.of(new Reservation()));
        assertFalse(reservationService.getAll().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void filtrer_CallsRepository() {
        when(reservationRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(java.util.List.of(new Reservation()));

        var result = reservationService.filtrer("EN_ATTENTE", LocalDateTime.now(), LocalDateTime.now(), 1L, 1L);
        assertNotNull(result);
        verify(reservationRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }
}