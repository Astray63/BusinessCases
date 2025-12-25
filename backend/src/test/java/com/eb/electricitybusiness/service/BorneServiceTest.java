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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
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

    @Test
    void delete_WithActiveReservations_ThrowsException() {
        Long id = 1L;
        when(borneRepository.existsById(id)).thenReturn(true);
        when(reservationRepository.hasActiveReservations(id)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> borneService.delete(id));
        verify(borneRepository, never()).deleteById(id);
    }

    @Test
    void delete_NonExistingId_ThrowsException() {
        Long id = 999L;
        when(borneRepository.existsById(id)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> borneService.delete(id));
    }

    @Test
    void toggleOccupation_ValidId_ReturnsUpdatedDto() {
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);
        borne.setOccupee(false);
        borne.setEtat(Borne.Etat.DISPONIBLE);

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));
        when(borneRepository.save(any(Borne.class))).thenAnswer(i -> i.getArgument(0));

        BorneDto result = borneService.toggleOccupation(id, true);

        assertNotNull(result);
        assertTrue(result.getOccupee());
        assertEquals("OCCUPEE", result.getEtat());
    }

    @Test
    void changerEtat_ValidId_ReturnsUpdatedDto() {
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);
        borne.setOccupee(false);
        borne.setEtat(Borne.Etat.DISPONIBLE);

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));
        when(borneRepository.save(any(Borne.class))).thenAnswer(i -> i.getArgument(0));

        BorneDto result = borneService.changerEtat(id, "EN_PANNE");

        assertNotNull(result);
        assertEquals("EN_PANNE", result.getEtat());
        assertEquals(Boolean.FALSE, result.getOccupee());
        // Note: changerEtat logic: if OCCUPEE -> occupée=true, if DISPONIBLE ->
        // occupée=false, else unchanged?
        // Looking at code: if OCCUPEE -> occupée=true, if DISPONIBLE -> occupée=false.
        // PANNE just changes enum.
    }

    @Test
    void changerEtat_Occupied_SetsOccupiedTrue() {
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));
        when(borneRepository.save(any(Borne.class))).thenAnswer(i -> i.getArgument(0));

        BorneDto result = borneService.changerEtat(id, "OCCUPEE");
        assertTrue(result.getOccupee());
    }

    @Test
    void getByDisponibilite_ReturnsAvailableList() {
        Borne borne = new Borne();
        borne.setIdBorne(1L);
        borne.setEtat(Borne.Etat.DISPONIBLE);

        when(borneRepository.findByEtat(Borne.Etat.DISPONIBLE)).thenReturn(Arrays.asList(borne));

        List<BorneDto> result = borneService.getByDisponibilite(true);
        assertFalse(result.isEmpty());
        assertEquals("DISPONIBLE", result.get(0).getEtat());
    }

    @Test
    void getByEtat_ReturnsList() {
        Borne borne = new Borne();
        borne.setIdBorne(1L);
        borne.setEtat(Borne.Etat.EN_PANNE);

        when(borneRepository.findByEtat(Borne.Etat.EN_PANNE)).thenReturn(Arrays.asList(borne));

        List<BorneDto> result = borneService.getByEtat("EN_PANNE");
        assertFalse(result.isEmpty());
        assertEquals("EN_PANNE", result.get(0).getEtat());
    }

    @Test
    void getProches_ReturnsList() {
        when(borneRepository.findByDistance(any(), any(), any())).thenReturn(Arrays.asList(new Borne()));
        assertFalse(borneService.getProches(1.0, 1.0, 10.0).isEmpty());
    }

    @Test
    void getByLieu_ReturnsList() {
        when(borneRepository.findByLieuxId(1L)).thenReturn(Arrays.asList(new Borne()));
        assertFalse(borneService.getByLieu(1L).isEmpty());
    }

    @Test
    void getBornesByOwnerDto_ReturnsList() {
        when(borneRepository.findByOwnerIdUtilisateur(1L)).thenReturn(Arrays.asList(new Borne()));
        assertFalse(borneService.getBornesByOwnerDto(1L).isEmpty());
    }

    @Test
    void getAllBornesDto_ReturnsList() {
        when(borneRepository.findAll()).thenReturn(Arrays.asList(new Borne()));
        assertFalse(borneService.getAllBornesDto().isEmpty());
    }

    @Test
    void searchAdvanced_FiltersByAllCriteria() {
        Borne b1 = new Borne();
        b1.setIdBorne(1L);
        b1.setPrixALaMinute(new BigDecimal("0.5")); // 30 per hour
        b1.setPuissance(50);
        b1.setEtat(Borne.Etat.DISPONIBLE);
        b1.setOccupee(false);

        Borne b2 = new Borne();
        b2.setIdBorne(2L);
        b2.setPrixALaMinute(new BigDecimal("1.0")); // 60 per hour
        b2.setPuissance(20);
        b2.setEtat(Borne.Etat.OCCUPEE);
        b2.setOccupee(true);

        when(borneRepository.findAll()).thenReturn(Arrays.asList(b1, b2));

        // Test filtering by price min
        List<BorneDto> results = borneService.searchAdvanced(null, null, null, new BigDecimal("40"), null, null, null,
                null);
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).getId());

        // Test filtering by price max
        results = borneService.searchAdvanced(null, null, null, null, new BigDecimal("40"), null, null, null);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());

        // Test filtering by power
        results = borneService.searchAdvanced(null, null, null, null, null, 40, null, null);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());

        // Test filtering by state
        results = borneService.searchAdvanced(null, null, null, null, null, null, "DISPONIBLE", null);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());

        // Test filtering by availability
        results = borneService.searchAdvanced(null, null, null, null, null, null, null, true);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void uploadPhotos_ValidPhotos_ReturnsUrls(@TempDir Path tempDir) throws Exception {
        Long id = 1L;
        Borne borne = new Borne();
        borne.setIdBorne(id);
        borne.setMedias(new java.util.ArrayList<>());

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));

        ReflectionTestUtils.setField(borneService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(borneService, "uploadBaseUrl", "http://localhost:8080/uploads/bornes");

        MockMultipartFile file = new MockMultipartFile("photos", "test.jpg", "image/jpeg", "test data".getBytes());

        List<String> urls = borneService.uploadPhotos(id, new MultipartFile[] { file });

        assertFalse(urls.isEmpty());
        assertTrue(urls.get(0).contains("http://localhost:8080/uploads/bornes/borne-1/"));
        verify(borneRepository).save(borne);
    }

    @Test
    void deletePhoto_ValidUrl_RemovesPhoto(@TempDir Path tempDir) throws Exception {
        Long id = 1L;
        String filename = "test.jpg";
        String url = "http://localhost:8080/uploads/bornes/borne-1/" + filename;

        Borne borne = new Borne();
        borne.setIdBorne(id);
        borne.setMedias(new java.util.ArrayList<>(List.of(url)));

        when(borneRepository.findById(id)).thenReturn(Optional.of(borne));

        // Setup file
        Path borneDir = tempDir.resolve("borne-1");
        java.nio.file.Files.createDirectories(borneDir);
        Path file = borneDir.resolve(filename);
        java.nio.file.Files.createFile(file);

        ReflectionTestUtils.setField(borneService, "uploadDir", tempDir.toString());

        borneService.deletePhoto(id, url);

        assertFalse(borne.getMedias().contains(url));
        assertFalse(java.nio.file.Files.exists(file));
        verify(borneRepository).save(borne);
    }
}
