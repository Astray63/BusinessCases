package com.electriccharge.app.service;

import com.electriccharge.app.dto.AuthRequestDto;
import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.exception.DuplicateResourceException;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void creerUtilisateur_ValidData_ReturnsCreatedUser() {
        // Arrange
        String motDePasse = "password123";
        String motDePasseEncode = "encoded_password";
        UtilisateurDto dto = new UtilisateurDto(
            null, // idUtilisateur
            "client", // role
            "John", // nom
            "Doe", // prenom
            "johndoe", // pseudo
            "john.doe@example.com", // email
            LocalDate.of(1990, 1, 1), // dateNaissance
            "FR123456789", // iban
            "123 Main St", // adressePhysique
            "avatar.jpg", // medias
            1L, // idAdresse
            true, // actif
            null, // dateCreation
            null  // dateModification
        );

        when(utilisateurRepository.existsByEmail(dto.email())).thenReturn(false);
        when(utilisateurRepository.existsByPseudo(dto.pseudo())).thenReturn(false);
        when(passwordEncoder.encode(motDePasse)).thenReturn(motDePasseEncode);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(i -> {
            Utilisateur u = (Utilisateur) i.getArguments()[0];
            u.setIdUtilisateur(1L);
            return u;
        });

        // Act
        UtilisateurDto result = utilisateurService.creerUtilisateur(dto, motDePasse);

        // Assert
        assertNotNull(result);
        assertEquals(dto.nom(), result.nom());
        assertEquals(dto.email(), result.email());
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void creerUtilisateur_DuplicateEmail_ThrowsDuplicateResourceException() {
        // Arrange
        UtilisateurDto dto = new UtilisateurDto(
            null,
            "client",
            "John",
            "Doe",
            "johndoe",
            "existing@example.com",
            LocalDate.of(1990, 1, 1),
            "FR123456789",
            "123 Main St",
            "profile.jpg",
            1L,
            true,
            null,
            null
        );

        when(utilisateurRepository.existsByEmail(dto.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
            () -> utilisateurService.creerUtilisateur(dto, "password"));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void getUtilisateurById_ExistingId_ReturnsUser() {
        // Arrange
        Long id = 1L;
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(id);
        utilisateur.setNom("John");
        utilisateur.setPrenom("Doe");

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateur));

        // Act
        UtilisateurDto result = utilisateurService.getUtilisateurById(id);

        // Assert
        assertNotNull(result);
        assertEquals(utilisateur.getNom(), result.nom());
        assertEquals(utilisateur.getPrenom(), result.prenom());
    }

    @Test
    void getUtilisateurById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(utilisateurRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> utilisateurService.getUtilisateurById(id));
    }

    @Test
    void getAllUtilisateurs_ReturnsAllUsers() {
        // Arrange
        Utilisateur user1 = new Utilisateur();
        user1.setIdUtilisateur(1L);
        user1.setNom("John");

        Utilisateur user2 = new Utilisateur();
        user2.setIdUtilisateur(2L);
        user2.setNom("Jane");

        when(utilisateurRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UtilisateurDto> result = utilisateurService.getAllUtilisateurs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).nom());
        assertEquals("Jane", result.get(1).nom());
    }

    @Test
    void validerMotDePasse_ValidCredentials_ReturnsTrue() {
        // Arrange
        String pseudo = "johndoe";
        String password = "password123";
        AuthRequestDto authRequest = new AuthRequestDto(pseudo, password);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPseudo(pseudo);
        utilisateur.setMotDePasse("encoded_password");

        when(utilisateurRepository.findByPseudo(pseudo)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches(password, utilisateur.getMotDePasse())).thenReturn(true);

        // Act
        boolean result = utilisateurService.validerMotDePasse(authRequest);

        // Assert
        assertTrue(result);
    }

    @Test
    void banirUtilisateur_ValidId_BansUser() {
        // Arrange
        Long id = 1L;
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(id);
        utilisateur.setEstBanni(false);

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        utilisateurService.banirUtilisateur(id);

        // Assert
        verify(utilisateurRepository).save(any(Utilisateur.class));
        assertTrue(utilisateur.getEstBanni());
    }
}
