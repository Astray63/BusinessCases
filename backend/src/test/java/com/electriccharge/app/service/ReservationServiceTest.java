package com.electriccharge.app.service;

import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.exception.DuplicateResourceException;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Reservation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Utilisateur utilisateur;
    private ChargingStation station;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(1L);

        station = new ChargingStation();
        station.setIdBorne(1L);
        station.setPrixALaMinute(new BigDecimal("0.50"));
    }

    @Test
    void create_ValidReservation_ReturnsDto() {
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(1L);
        dto.setChargingStationId(1L);
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation saved = i.getArgument(0);
            saved.setNumeroReservation(1L);
            return saved;
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
        dto.setChargingStationId(1L);
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Collections.singletonList(new Reservation()));

        assertThrows(DuplicateResourceException.class, () -> reservationService.create(dto));
        verify(reservationRepository, never()).save(any());
    }
} 