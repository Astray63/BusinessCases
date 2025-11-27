package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.service.impl.BorneServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class BorneServiceTest {

    @Mock
    private BorneRepository borneRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private com.eb.electricitybusiness.repository.LieuRepository lieuRepository;

    @InjectMocks
    private BorneServiceImpl borneService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ValidDto_ReturnsCreatedBorne() {
        // Arrange
        BorneDto dto = new BorneDto();
        dto.setNom("Test Borne");
        dto.setNumero("123");
        dto.setLocalisation("Test Location");
        dto.setLatitude(45.0);
        dto.setLongitude(5.0);
        dto.setPuissance(50);
        dto.setEtat("DISPONIBLE");
        dto.setOccupee(false);
        dto.setPrixALaMinute(new BigDecimal("2.50"));
        dto.setOwnerId(1L);
        dto.setLieuId(1L);

        Utilisateur owner = new Utilisateur();
        owner.setIdUtilisateur(1L);

        com.eb.electricitybusiness.model.Lieu lieu = new com.eb.electricitybusiness.model.Lieu();
        lieu.setIdLieu(1L);

        Borne savedBorne = new Borne();
        savedBorne.setIdBorne(1L);
        savedBorne.setNom(dto.getNom());
        savedBorne.setOwner(owner);
        savedBorne.setLieu(lieu);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(lieuRepository.findById(1L)).thenReturn(Optional.of(lieu));
        when(borneRepository.save(any(Borne.class))).thenReturn(savedBorne);

        // Act
        BorneDto result = borneService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getNom(), result.getNom());
        assertEquals(1L, result.getOwnerId());
        verify(borneRepository, times(1)).save(any(Borne.class));
    }

    @Test
    void create_InvalidOwnerId_ThrowsEntityNotFoundException() {
        // Arrange
        BorneDto dto = new BorneDto();
        dto.setOwnerId(999L);

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> borneService.create(dto));
        verify(borneRepository, never()).save(any(Borne.class));
    }

    @Test
    void getById_ExistingId_ReturnsBorne() {
        // Arrange
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);
        borne.setNom("Test Borne");

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));

        // Act
        Borne result = borneService.getBorneById(id);

        // Assert
        assertNotNull(result);
        assertEquals(borne.getNom(), result.getNom());
        assertEquals(borne.getIdBorne(), id);
    }

    @Test
    void getById_NonExistingId_ThrowsEntityNotFoundException() {
        // Arrange
        Long id = 999L;
        when(borneRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> borneService.getBorneById(id));
    }

    @Test
    void getAll_ReturnsAllBornes() {
        // Arrange
        Borne borne1 = new Borne();
        borne1.setIdBorne(1L);
        borne1.setNom("Borne 1");

        Borne borne2 = new Borne();
        borne2.setIdBorne(2L);
        borne2.setNom("Borne 2");

        when(borneRepository.findAll()).thenReturn(Arrays.asList(borne1, borne2));

        // Act
        List<Borne> result = borneService.getAllBornes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Borne 1", result.get(0).getNom());
        assertEquals("Borne 2", result.get(1).getNom());
    }

    @Test
    void update_ValidIdAndDto_ReturnsUpdatedBorne() {
        // Arrange
        Long id = 1L;
        BorneDto dto = new BorneDto();
        dto.setNom("Updated Borne");
        dto.setOwnerId(1L);
        dto.setLieuId(1L);

        Borne existingBorne = new Borne();
        existingBorne.setIdBorne(id);
        existingBorne.setNom("Old Borne");

        Utilisateur owner = new Utilisateur();
        owner.setIdUtilisateur(1L);

        com.eb.electricitybusiness.model.Lieu lieu = new com.eb.electricitybusiness.model.Lieu();
        lieu.setIdLieu(1L);

        when(borneRepository.findById(id)).thenReturn(Optional.of(existingBorne));
        when(borneRepository.existsById(id)).thenReturn(true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(lieuRepository.findById(1L)).thenReturn(Optional.of(lieu));
        when(borneRepository.save(any(Borne.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        BorneDto result = borneService.update(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Borne", result.getNom());
        verify(borneRepository, times(1)).save(any(Borne.class));
    }

    @Test
    void delete_ValidId_DeletesBorne() {
        // Arrange
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);

        when(borneRepository.existsById(id)).thenReturn(true);

        // Act
        borneService.delete(id);

        // Assert
        verify(borneRepository, times(1)).deleteById(id);
    }

    @Test
    void getByOwner_ValidOwnerId_ReturnsBornes() {
        // Arrange
        Long ownerId = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(1L);
        borne.setNom("Borne 1");

        when(borneRepository.findByOwnerIdUtilisateur(ownerId)).thenReturn(Arrays.asList(borne));

        // Act
        List<Borne> result = borneService.getBornesByOwner(ownerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Borne 1", result.get(0).getNom());
    }

}
