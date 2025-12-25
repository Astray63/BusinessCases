package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.dto.DashboardStatsDto;
import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BorneRepository borneRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private BorneService borneService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDashboardStats_ClientOnly_ReturnsClientStats() {
        Long userId = 1L;

        // Mock bornes (empty for client only)
        when(borneRepository.findByOwnerIdUtilisateur(userId)).thenReturn(Collections.emptyList());

        // Mock reservations
        Reservation r1 = new Reservation();
        r1.setEtat(Reservation.EtatReservation.ACTIVE);
        r1.setTotalPrice(BigDecimal.TEN);
        r1.setCreatedAt(LocalDateTime.now());
        r1.setDateDebut(LocalDateTime.now().plusDays(1));
        r1.setNumeroReservation(100L); // Ensure ID is set

        Reservation r2 = new Reservation();
        r2.setEtat(Reservation.EtatReservation.TERMINEE);
        r2.setTotalPrice(BigDecimal.valueOf(20));
        r2.setCreatedAt(LocalDateTime.now().minusMonths(1));
        r2.setDateDebut(LocalDateTime.now().minusMonths(1));

        when(reservationRepository.findByUtilisateur_IdUtilisateur(userId)).thenReturn(Arrays.asList(r1, r2));

        // Mock reservation service for recent list and next reservation details
        ReservationDto r1Dto = new ReservationDto();
        r1Dto.setId(100L);
        when(reservationService.getByUser(userId)).thenReturn(Arrays.asList(r1Dto));
        when(reservationService.getById(100L)).thenReturn(r1Dto);

        DashboardStatsDto result = dashboardService.getDashboardStats(userId);

        assertNotNull(result);
        assertNotNull(result.getClientStats());
        assertNull(result.getOwnerStats());
        assertEquals(2, result.getClientStats().getTotalReservations());
        assertEquals(BigDecimal.valueOf(30), result.getClientStats().getMontantTotalDepense());
        assertNotNull(result.getClientStats().getProchaineReservation());
    }

    @Test
    void getDashboardStats_Owner_ReturnsOwnerStats() {
        Long userId = 1L;

        // Mock bornes
        Borne b1 = new Borne();
        b1.setIdBorne(10L);
        b1.setEtat(Borne.Etat.DISPONIBLE);
        b1.setOwner(new Utilisateur());
        b1.getOwner().setIdUtilisateur(userId);

        when(borneRepository.findByOwnerIdUtilisateur(userId)).thenReturn(Arrays.asList(b1));
        when(borneService.getBornesByOwner(userId)).thenReturn(Arrays.asList(b1));

        // Mock reservations on borne
        Reservation r1 = new Reservation();
        r1.setEtat(Reservation.EtatReservation.ACTIVE);
        r1.setTotalPrice(BigDecimal.TEN);
        r1.setCreatedAt(LocalDateTime.now());
        r1.setDateDebut(LocalDateTime.now().plusDays(1));
        r1.setBorne(b1);

        when(reservationRepository.findByBorneIdBorne(10L)).thenReturn(Arrays.asList(r1));

        // Mock calls for client stats (empty)
        when(reservationRepository.findByUtilisateur_IdUtilisateur(userId)).thenReturn(Collections.emptyList());
        when(reservationService.getByUser(userId)).thenReturn(Collections.emptyList());

        DashboardStatsDto result = dashboardService.getDashboardStats(userId);

        assertNotNull(result);
        assertNotNull(result.getOwnerStats());
        assertEquals(1, result.getOwnerStats().getTotalBornes());
        assertEquals(1, result.getOwnerStats().getBornesDisponibles());
        assertEquals(1, result.getOwnerStats().getTotalReservations());
        assertEquals(BigDecimal.TEN, result.getOwnerStats().getRevenusEstimesMois());
    }

    @Test
    void getDashboardStats_MostReservedBorne_CalculatesCorrectly() {
        Long userId = 1L;

        Borne b1 = new Borne();
        b1.setIdBorne(10L);
        b1.setEtat(Borne.Etat.DISPONIBLE);
        b1.setOwner(new Utilisateur());
        b1.getOwner().setIdUtilisateur(userId);

        Borne b2 = new Borne();
        b2.setIdBorne(20L);
        b2.setEtat(Borne.Etat.DISPONIBLE);
        b2.setOwner(new Utilisateur());
        b2.getOwner().setIdUtilisateur(userId);

        when(borneRepository.findByOwnerIdUtilisateur(userId)).thenReturn(Arrays.asList(b1, b2));
        when(borneService.getBornesByOwner(userId)).thenReturn(Arrays.asList(b1, b2));

        // b1 has 2 reservations, b2 has 1
        Reservation r1 = new Reservation();
        r1.setBorne(b1);
        r1.setEtat(Reservation.EtatReservation.TERMINEE);
        Reservation r2 = new Reservation();
        r2.setBorne(b1);
        r2.setEtat(Reservation.EtatReservation.TERMINEE);
        Reservation r3 = new Reservation();
        r3.setBorne(b2);
        r3.setEtat(Reservation.EtatReservation.TERMINEE);

        when(reservationRepository.findByBorneIdBorne(10L)).thenReturn(Arrays.asList(r1, r2));
        when(reservationRepository.findByBorneIdBorne(20L)).thenReturn(Arrays.asList(r3));

        when(borneService.getBorneById(10L)).thenReturn(b1);

        DashboardStatsDto result = dashboardService.getDashboardStats(userId);

        assertNotNull(result.getOwnerStats().getBorneLaPlusReservee());
        assertEquals(10L, result.getOwnerStats().getBorneLaPlusReservee().getId());
    }

    @Test
    void getDashboardStats_HandleExceptionsInSubServiceCalls() {
        Long userId = 1L;

        when(borneRepository.findByOwnerIdUtilisateur(userId)).thenReturn(Collections.emptyList());

        Reservation r1 = new Reservation();
        r1.setEtat(Reservation.EtatReservation.ACTIVE);
        r1.setDateDebut(LocalDateTime.now().plusHours(1));
        r1.setNumeroReservation(100L);

        when(reservationRepository.findByUtilisateur_IdUtilisateur(userId)).thenReturn(Arrays.asList(r1));

        // Throw exception when fetching reservation details
        when(reservationService.getById(100L)).thenThrow(new RuntimeException("Service failure"));

        DashboardStatsDto result = dashboardService.getDashboardStats(userId);

        // Should not fail, just skip nextReservation
        assertNotNull(result.getClientStats());
        assertNull(result.getClientStats().getProchaineReservation());
    }

    @Test
    void getDashboardStats_Owner_MostReservedBorneException_Handled() {
        Long userId = 1L;

        Borne b1 = new Borne();
        b1.setIdBorne(10L);
        b1.setEtat(Borne.Etat.DISPONIBLE);
        b1.setOwner(new Utilisateur());
        b1.getOwner().setIdUtilisateur(userId);

        when(borneRepository.findByOwnerIdUtilisateur(userId)).thenReturn(Arrays.asList(b1));
        when(borneService.getBornesByOwner(userId)).thenReturn(Arrays.asList(b1));

        Reservation r1 = new Reservation();
        r1.setBorne(b1);
        when(reservationRepository.findByBorneIdBorne(10L)).thenReturn(Arrays.asList(r1));

        when(borneService.getBorneById(10L)).thenThrow(new RuntimeException("DB Error"));

        DashboardStatsDto result = dashboardService.getDashboardStats(userId);

        assertNotNull(result.getOwnerStats());
        // Borne la plus réservée should be null because of exception
        assertNull(result.getOwnerStats().getBorneLaPlusReservee());
    }
}
