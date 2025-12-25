package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.CreateSignalementDto;
import com.eb.electricitybusiness.dto.SignalementDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.model.Signalement;
import com.eb.electricitybusiness.model.Signalement.StatutSignalement;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.repository.SignalementRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SignalementServiceTest {

    @Mock
    private SignalementRepository signalementRepository;
    @Mock
    private BorneRepository borneRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private SignalementService signalementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getSignalementsByChargingStation_ReturnsList() {
        Long stationId = 1L;
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        u.setPseudo("p");
        u.setNom("n");
        u.setPrenom("p");
        Borne b = new Borne();
        b.setIdBorne(stationId);
        b.setNom("b");

        Signalement s = new Signalement();
        s.setIdSignalement(10L);
        s.setUser(u);
        s.setBorne(b);
        s.setStatut(StatutSignalement.OUVERT);

        when(signalementRepository.findByBorneIdBorneOrderByDateSignalementDesc(stationId))
                .thenReturn(Arrays.asList(s));

        List<SignalementDto> result = signalementService.getSignalementsByChargingStation(stationId);
        assertEquals(1, result.size());
    }

    @Test
    void getSignalementsByUser_WithId_ReturnsList() {
        when(signalementRepository.findByUserIdUtilisateurOrderByDateSignalementDesc(1L)).thenReturn(Arrays.asList());
        List<SignalementDto> result = signalementService.getSignalementsByUser(1L);
        assertNotNull(result);
    }

    @Test
    void getSignalementsByUser_NullId_UsesAuthContext() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(2L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        signalementService.getSignalementsByUser(null);
        verify(signalementRepository).findByUserIdUtilisateurOrderByDateSignalementDesc(2L);
    }

    @Test
    void getSignalementsByStatut_ReturnsList() {
        when(signalementRepository.findByStatutOrderByDateSignalementDesc(StatutSignalement.OUVERT))
                .thenReturn(Arrays.asList());
        assertNotNull(signalementService.getSignalementsByStatut(StatutSignalement.OUVERT));
    }

    @Test
    void createSignalement_Success() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        u.setPseudo(pseudo);
        u.setNom("n");
        u.setPrenom("p");
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Borne b = new Borne();
        b.setIdBorne(10L);
        b.setNom("b");
        b.setNumero("123");
        when(borneRepository.findById(10L)).thenReturn(Optional.of(b));

        CreateSignalementDto dto = new CreateSignalementDto();
        dto.setChargingStationId(10L);
        dto.setDescription("Broken");
        dto.setReservationId(100L);

        Reservation r = new Reservation();
        r.setNumeroReservation(100L);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));

        when(signalementRepository.save(any(Signalement.class))).thenAnswer(i -> {
            Signalement s = i.getArgument(0);
            s.setIdSignalement(55L);
            return s;
        });

        SignalementDto result = signalementService.createSignalement(dto);
        assertNotNull(result);
        assertEquals(100L, result.getReservationId());
    }

    @Test
    void updateStatut_ToResolu_UpdatesStatutAndDate() {
        Signalement s = new Signalement();
        s.setIdSignalement(1L);
        Utilisateur u = new Utilisateur();
        u.setPseudo("p");
        u.setNom("n");
        u.setPrenom("p");
        s.setUser(u);
        Borne b = new Borne();
        b.setIdBorne(1L);
        b.setNom("b");
        s.setBorne(b);

        when(signalementRepository.findById(1L)).thenReturn(Optional.of(s));
        when(signalementRepository.save(any(Signalement.class))).thenAnswer(i -> i.getArgument(0));

        SignalementDto result = signalementService.updateStatut(1L, StatutSignalement.RESOLU);

        assertEquals(StatutSignalement.RESOLU, result.getStatut());
        assertNotNull(s.getDateResolution());
    }

    @Test
    void deleteSignalement_AsAuthor_Success() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Signalement s = new Signalement();
        s.setUser(u);
        when(signalementRepository.findById(1L)).thenReturn(Optional.of(s));

        signalementService.deleteSignalement(1L);
        verify(signalementRepository).delete(s);
    }

    @Test
    void deleteSignalement_NotAuthor_ThrowsException() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Utilisateur other = new Utilisateur();
        other.setIdUtilisateur(2L);
        Signalement s = new Signalement();
        s.setUser(other);
        when(signalementRepository.findById(1L)).thenReturn(Optional.of(s));

        assertThrows(RuntimeException.class, () -> signalementService.deleteSignalement(1L));
    }

    @Test
    void countOpenSignalements_ReturnsCount() {
        when(signalementRepository.countByBorneIdBorneAndStatut(1L, StatutSignalement.OUVERT)).thenReturn(5L);
        assertEquals(5L, signalementService.countOpenSignalements(1L));
    }
}
