package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.model.ChargingStation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.ChargingStationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.service.impl.ChargingStationServiceImpl;
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

class ChargingStationServiceTest {

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ChargingStationServiceImpl chargingStationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ValidDto_ReturnsCreatedStation() {
        // Arrange
        ChargingStationDto dto = new ChargingStationDto();
        dto.setNom("Test Station");
        dto.setNumero("123");
        dto.setLocalisation("Test Location");
        dto.setLatitude(45.0);
        dto.setLongitude(5.0);
        dto.setPuissance(50);
        dto.setEtat("DISPONIBLE");
        dto.setOccupee(false);
        dto.setPrixALaMinute(new BigDecimal("2.50"));
        dto.setAddress("123 Test Street");
        dto.setOwnerId(1L);

        Utilisateur owner = new Utilisateur();
        owner.setIdUtilisateur(1L);

        ChargingStation savedStation = new ChargingStation();
        savedStation.setIdBorne(1L);
        savedStation.setNom(dto.getNom());
        savedStation.setAddress(dto.getAddress());
        savedStation.setOwner(owner);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(savedStation);

        // Act
        ChargingStationDto result = chargingStationService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getNom(), result.getNom());
        assertEquals(dto.getAddress(), result.getAddress());
        assertEquals(1L, result.getOwnerId());
        verify(chargingStationRepository, times(1)).save(any(ChargingStation.class));
    }

    @Test
    void changerEtat_ValidIdAndState_ChangesState() {
        // Arrange
        Long id = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(id);
        station.setEtat(ChargingStation.Etat.DISPONIBLE);

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(station));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ChargingStationDto result = chargingStationService.changerEtat(id, "EN_PANNE");

        // Assert
        assertNotNull(result);
        assertEquals(ChargingStation.Etat.EN_PANNE.name(), result.getEtat());
        verify(chargingStationRepository, times(1)).save(any(ChargingStation.class));
    }

    @Test
    void create_InvalidOwnerId_ThrowsEntityNotFoundException() {
        // Arrange
        ChargingStationDto dto = new ChargingStationDto();
        dto.setOwnerId(999L);

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> chargingStationService.create(dto));
        verify(chargingStationRepository, never()).save(any(ChargingStation.class));
    }

    @Test
    void getById_ExistingId_ReturnsStation() {
        // Arrange
        Long id = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(id);
        station.setNom("Test Station");

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(station));

        // Act
        ChargingStationDto result = chargingStationService.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(station.getNom(), result.getNom());
        assertEquals(station.getIdBorne(), id);
    }

    @Test
    void getById_NonExistingId_ThrowsEntityNotFoundException() {
        // Arrange
        Long id = 999L;
        when(chargingStationRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> chargingStationService.getById(id));
    }

    @Test
    void getAll_ReturnsAllStations() {
        // Arrange
        ChargingStation station1 = new ChargingStation();
        station1.setIdBorne(1L);
        station1.setNom("Station 1");

        ChargingStation station2 = new ChargingStation();
        station2.setIdBorne(2L);
        station2.setNom("Station 2");

        when(chargingStationRepository.findAll()).thenReturn(Arrays.asList(station1, station2));

        // Act
        List<ChargingStationDto> result = chargingStationService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Station 1", result.get(0).getNom());
        assertEquals("Station 2", result.get(1).getNom());
    }

    @Test
    void toggleOccupation_ValidId_TogglesOccupationStatus() {
        // Arrange
        Long id = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(id);
        station.setOccupee(false);

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(station));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ChargingStationDto result = chargingStationService.toggleOccupation(id, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOccupee());
        verify(chargingStationRepository, times(1)).save(any(ChargingStation.class));
    }

    @Test
    void update_ValidIdAndDto_ReturnsUpdatedStation() {
        // Arrange
        Long id = 1L;
        ChargingStationDto dto = new ChargingStationDto();
        dto.setNom("Updated Station");
        dto.setOwnerId(1L);

        ChargingStation existingStation = new ChargingStation();
        existingStation.setIdBorne(id);
        existingStation.setNom("Old Station");

        Utilisateur owner = new Utilisateur();
        owner.setIdUtilisateur(1L);

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(existingStation));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ChargingStationDto result = chargingStationService.update(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Station", result.getNom());
        verify(chargingStationRepository, times(1)).save(any(ChargingStation.class));
    }

    @Test
    void delete_ValidId_DeletesStation() {
        // Arrange
        Long id = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(id);

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(station));
        when(reservationRepository.hasActiveReservations(id)).thenReturn(false);

        // Act
        chargingStationService.delete(id);

        // Assert
        verify(chargingStationRepository, times(1)).deleteById(id);
    }

    @Test
    void delete_ActiveReservations_ThrowsIllegalStateException() {
        // Arrange
        Long id = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(id);

        when(chargingStationRepository.findById(id)).thenReturn(Optional.of(station));
        when(reservationRepository.hasActiveReservations(id)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> chargingStationService.delete(id));
        verify(chargingStationRepository, never()).deleteById(anyLong());
    }

    @Test
    void getByOwner_ValidOwnerId_ReturnsStations() {
        // Arrange
        Long ownerId = 1L;
        ChargingStation station = new ChargingStation();
        station.setIdBorne(1L);
        station.setNom("Station 1");

        when(chargingStationRepository.findByOwner_IdUtilisateur(ownerId)).thenReturn(Arrays.asList(station));

        // Act
        List<ChargingStationDto> result = chargingStationService.getByOwner(ownerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Station 1", result.get(0).getNom());
    }

    @Test
    void searchAdvanced_WithFilters_ReturnsFilteredStations() {
        // Arrange
        ChargingStation station1 = new ChargingStation();
        station1.setIdBorne(1L);
        station1.setHourlyRate(new BigDecimal("10.00"));
        station1.setPuissance(50);
        station1.setEtat(ChargingStation.Etat.DISPONIBLE);

        ChargingStation station2 = new ChargingStation();
        station2.setIdBorne(2L);
        station2.setHourlyRate(new BigDecimal("20.00"));
        station2.setPuissance(22);
        station2.setEtat(ChargingStation.Etat.OCCUPEE);

        when(chargingStationRepository.findAll()).thenReturn(Arrays.asList(station1, station2));

        // Act
        List<ChargingStationDto> result = chargingStationService.searchAdvanced(
                null, null, null,
                new BigDecimal("5.00"), new BigDecimal("15.00"),
                40, "DISPONIBLE", true
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
