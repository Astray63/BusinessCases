package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.AvisDto;
import com.eb.electricitybusiness.dto.CreateAvisDto;
import com.eb.electricitybusiness.model.Avis;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.AvisRepository;
import com.eb.electricitybusiness.repository.BorneRepository;
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

class AvisServiceTest {

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private BorneRepository borneRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AvisService avisService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

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
    void getAvisByChargingStation_ReturnsList() {
        Long stationId = 1L;
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        u.setPseudo("p");
        u.setNom("n");
        u.setPrenom("prenom");
        Borne b = new Borne();
        b.setIdBorne(stationId);
        b.setNom("b");

        Avis avis = new Avis();
        avis.setIdAvis(10L);
        avis.setNote(5);
        avis.setUtilisateur(u);
        avis.setBorne(b);
        avis.setCreatedAt(LocalDateTime.now());

        when(avisRepository.findByBorneIdBorneOrderByCreatedAtDesc(stationId)).thenReturn(Arrays.asList(avis));

        List<AvisDto> result = avisService.getAvisByChargingStation(stationId);

        assertFalse(result.isEmpty());
        assertEquals(10L, result.get(0).getIdAvis());
        assertEquals("p", result.get(0).getUtilisateurPseudo());
    }

    @Test
    void getAvisByUser_WithId_ReturnsList() {
        Long userId = 1L;
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        u.setPseudo("p");
        u.setNom("n");
        u.setPrenom("prenom");
        Borne b = new Borne();
        b.setIdBorne(10L);
        b.setNom("b");

        Avis avis = new Avis();
        avis.setUtilisateur(u);
        avis.setBorne(b);

        when(avisRepository.findByUtilisateurIdUtilisateurOrderByCreatedAtDesc(userId)).thenReturn(Arrays.asList(avis));

        List<AvisDto> result = avisService.getAvisByUser(userId);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAvisByUser_NullId_UsesAuthContext() {
        String pseudo = "testuser";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(99L);
        u.setPseudo(pseudo);
        u.setNom("n");
        u.setPrenom("p");
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Borne b = new Borne();
        b.setIdBorne(10L);
        b.setNom("b");
        Avis avis = new Avis();
        avis.setUtilisateur(u);
        avis.setBorne(b);

        when(avisRepository.findByUtilisateurIdUtilisateurOrderByCreatedAtDesc(99L)).thenReturn(Arrays.asList(avis));

        List<AvisDto> result = avisService.getAvisByUser(null);

        assertFalse(result.isEmpty());
        verify(utilisateurRepository).findByPseudo(pseudo);
    }

    @Test
    void getAverageNoteByChargingStation_ReturnsAverage() {
        when(avisRepository.getAverageNoteByBorne(1L)).thenReturn(4.56);
        assertEquals(4.6, avisService.getAverageNoteByChargingStation(1L));
    }

    @Test
    void getAverageNoteByChargingStation_Null_ReturnsZero() {
        when(avisRepository.getAverageNoteByBorne(1L)).thenReturn(null);
        assertEquals(0.0, avisService.getAverageNoteByChargingStation(1L));
    }

    @Test
    void createAvis_Success() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        u.setPseudo(pseudo);
        u.setNom("n");
        u.setPrenom("p");
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        CreateAvisDto dto = new CreateAvisDto();
        dto.setChargingStationId(10L);
        dto.setNote(5);
        dto.setCommentaire("Good");

        Borne b = new Borne();
        b.setIdBorne(10L);
        b.setNom("Borne");
        b.setNumero("123");
        when(borneRepository.findById(10L)).thenReturn(Optional.of(b));

        when(avisRepository.existsByUtilisateurIdUtilisateurAndBorneIdBorne(1L, 10L)).thenReturn(false);

        when(avisRepository.save(any(Avis.class))).thenAnswer(i -> {
            Avis a = i.getArgument(0);
            a.setIdAvis(55L);
            return a;
        });

        AvisDto result = avisService.createAvis(dto);

        assertNotNull(result);
        verify(avisRepository).save(any(Avis.class));
    }

    @Test
    void createAvis_AlreadyExists_ThrowsException() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        CreateAvisDto dto = new CreateAvisDto();
        dto.setChargingStationId(10L);

        when(avisRepository.existsByUtilisateurIdUtilisateurAndBorneIdBorne(1L, 10L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> avisService.createAvis(dto));
    }

    @Test
    void deleteAvis_AsAuthor_Success() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Avis avis = new Avis();
        avis.setIdAvis(100L);
        avis.setUtilisateur(u);

        when(avisRepository.findById(100L)).thenReturn(Optional.of(avis));

        avisService.deleteAvis(100L);
        verify(avisRepository).delete(avis);
    }

    @Test
    void deleteAvis_NotAuthor_ThrowsException() {
        String pseudo = "user";
        when(authentication.getName()).thenReturn(pseudo);

        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(1L);
        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(u));

        Utilisateur other = new Utilisateur();
        other.setIdUtilisateur(2L);
        Avis avis = new Avis();
        avis.setIdAvis(100L);
        avis.setUtilisateur(other); // Different user

        when(avisRepository.findById(100L)).thenReturn(Optional.of(avis));

        assertThrows(RuntimeException.class, () -> avisService.deleteAvis(100L));
    }
}
