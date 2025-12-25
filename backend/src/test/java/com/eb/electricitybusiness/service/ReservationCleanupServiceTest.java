package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationCleanupServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationCleanupService reservationCleanupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cleanupExpiredPendingReservations_Found_CancelsAndSaves() {
        Reservation r1 = new Reservation();
        r1.setNumeroReservation(1L);
        r1.setEtat(Reservation.EtatReservation.EN_ATTENTE);

        when(reservationRepository.findExpiredPendingReservations(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(r1));

        reservationCleanupService.cleanupExpiredPendingReservations();

        verify(reservationRepository).saveAll(anyList());
        // Verify state changed
        assert (r1.getEtat() == Reservation.EtatReservation.ANNULEE);
    }

    @Test
    void cleanupExpiredPendingReservations_Empty_DoNothing() {
        when(reservationRepository.findExpiredPendingReservations(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reservationCleanupService.cleanupExpiredPendingReservations();

        verify(reservationRepository, never()).saveAll(anyList());
    }

    @Test
    void cleanupExpiredPendingReservations_Exception_LogsError() {
        when(reservationRepository.findExpiredPendingReservations(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("DB Error"));

        // Should not throw
        reservationCleanupService.cleanupExpiredPendingReservations();
    }

    @Test
    void cleanupPastPendingReservations_Found_CancelsAndSaves() {
        Reservation r1 = new Reservation();
        r1.setNumeroReservation(2L);
        r1.setEtat(Reservation.EtatReservation.EN_ATTENTE);

        when(reservationRepository.findPastPendingReservations(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(r1));

        reservationCleanupService.cleanupPastPendingReservations();

        verify(reservationRepository).saveAll(anyList());
        assert (r1.getEtat() == Reservation.EtatReservation.ANNULEE);
    }

    @Test
    void cleanupPastPendingReservations_Empty_DoNothing() {
        when(reservationRepository.findPastPendingReservations(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reservationCleanupService.cleanupPastPendingReservations();

        verify(reservationRepository, never()).saveAll(anyList());
    }

    @Test
    void cleanupPastPendingReservations_Exception_LogsError() {
        when(reservationRepository.findPastPendingReservations(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("DB Error"));

        // Should not throw
        reservationCleanupService.cleanupPastPendingReservations();
    }
}
