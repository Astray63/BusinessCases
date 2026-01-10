package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.LieuDto;
import com.eb.electricitybusiness.exception.ResourceNotFoundException;
import com.eb.electricitybusiness.model.Lieu;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.model.UtilisateurLieu;
import com.eb.electricitybusiness.repository.LieuRepository;
import com.eb.electricitybusiness.repository.UtilisateurLieuRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.impl.LieuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class LieuServiceTest {

    @Mock
    private LieuRepository lieuRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private UtilisateurLieuRepository utilisateurLieuRepository;

    @InjectMocks
    private LieuServiceImpl lieuService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ValidData_ReturnsCreatedLieu() {
        Long userId = 1L;
        LieuDto dto = new LieuDto(null, "Home", "123 St", "75000", "Paris", "France", 1.0, 2.0, null, null);
        Utilisateur user = new Utilisateur();
        user.setIdUtilisateur(userId);

        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lieuRepository.save(any(Lieu.class))).thenAnswer(i -> {
            Lieu l = i.getArgument(0);
            l.setIdLieu(10L);
            return l;
        });

        LieuDto result = lieuService.create(dto, userId);

        assertNotNull(result);
        assertEquals(10L, result.idLieu());
        assertEquals("Home", result.nom());
        verify(utilisateurLieuRepository).save(any(UtilisateurLieu.class));
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        LieuDto dto = new LieuDto(null, "Home", "123 St", "75000", "Paris", "France", 1.0, 2.0, null, null);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lieuService.create(dto, 1L));
    }

    @Test
    void update_ExistingId_ReturnsUpdatedLieu() {
        Long id = 10L;
        Lieu existing = new Lieu();
        existing.setIdLieu(id);
        LieuDto dto = new LieuDto(id, "Work", "456 Av", "75001", "Paris", "France", 3.0, 4.0, null, null);

        when(lieuRepository.findById(id)).thenReturn(Optional.of(existing));
        when(lieuRepository.save(any(Lieu.class))).thenAnswer(i -> i.getArgument(0));

        LieuDto result = lieuService.update(id, dto);

        assertEquals("Work", result.nom());
        assertEquals("456 Av", result.adresse());
    }

    @Test
    void update_NotFound_ThrowsException() {
        LieuDto dto = new LieuDto(10L, "Work", "456 Av", "75001", "Paris", "France", 3.0, 4.0, null, null);
        when(lieuRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lieuService.update(10L, dto));
    }

    @Test
    void delete_ExistingId_DeletesLieuAndRelations() {
        Long id = 10L;
        Lieu lieu = new Lieu();
        lieu.setIdLieu(id);

        when(lieuRepository.findById(id)).thenReturn(Optional.of(lieu));
        when(utilisateurLieuRepository.findByLieu_IdLieu(id)).thenReturn(Arrays.asList(new UtilisateurLieu()));

        lieuService.delete(id);

        verify(utilisateurLieuRepository).deleteAll(anyList());
        verify(lieuRepository).delete(lieu);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(lieuRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lieuService.delete(10L));
    }

    @Test
    void getById_Found_ReturnsDto() {
        Lieu lieu = new Lieu();
        lieu.setIdLieu(1L);
        lieu.setNom("Home");
        when(lieuRepository.findById(1L)).thenReturn(Optional.of(lieu));
        LieuDto result = lieuService.getById(1L);
        assertEquals("Home", result.nom());
    }

    @Test
    void getAll_ReturnsList() {
        when(lieuRepository.findAll()).thenReturn(Arrays.asList(new Lieu(), new Lieu()));
        List<LieuDto> result = lieuService.getAll();
        assertEquals(2, result.size());
    }

    @Test
    void getByUtilisateur_ReturnsList() {
        when(utilisateurLieuRepository.findLieuxByUtilisateurId(1L)).thenReturn(Arrays.asList(new Lieu()));
        List<LieuDto> result = lieuService.getByUtilisateur(1L);
        assertEquals(1, result.size());
    }

    @Test
    void searchByNom_ReturnsList() {
        when(lieuRepository.findByNomContainingIgnoreCase("Home")).thenReturn(Arrays.asList(new Lieu()));
        List<LieuDto> result = lieuService.searchByNom("Home");
        assertEquals(1, result.size());
    }

    @Test
    void getProches_ReturnsList() {
        // Le service convertit km en mètres (10.0 km * 1000 = 10000.0 mètres)
        when(lieuRepository.findByDistance(1.0, 1.0, 10000.0)).thenReturn(Arrays.asList(new Lieu()));
        List<LieuDto> result = lieuService.getProches(1.0, 1.0, 10.0);
        assertEquals(1, result.size());
    }
}
