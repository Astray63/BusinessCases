package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.AuthRequestDto;
import com.eb.electricitybusiness.dto.ChangePasswordRequestDto;
import com.eb.electricitybusiness.dto.UtilisateurDto;
import com.eb.electricitybusiness.exception.DuplicateResourceException;
import com.eb.electricitybusiness.exception.ResourceNotFoundException;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
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
                null, "client", "Test", "User", "testuser", "test@test.com",
                LocalDate.of(1990, 1, 1), "123 Rue Test", "0123456789", "75000", "Paris",
                null, true, null, null);

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
                null, "client", "Test", "User", "testuser", "existing@example.com",
                LocalDate.of(1990, 1, 1), "123 Rue Test", "0123456789", "75000", "Paris",
                null, true, null, null);

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
    void getUtilisateurByPseudo_ValidPseudo_ReturnsDto() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(1L);
        utilisateur.setPseudo("testuser");
        utilisateur.setRole(Utilisateur.Role.client);
        utilisateur.setNom("Test");
        utilisateur.setPrenom("User");
        utilisateur.setEmail("test@test.com");
        utilisateur.setDateCreation(LocalDateTime.now());

        when(utilisateurRepository.findByPseudo("testuser")).thenReturn(Optional.of(utilisateur));

        UtilisateurDto result = utilisateurService.getUtilisateurByPseudo("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.pseudo());
    }

    @Test
    void changePassword_ValidData_UpdatesPassword() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(1L);
        utilisateur.setMotDePasse("encodedOldPassword");

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");
        request.setConfirmationMotDePasse("newPassword");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        utilisateurService.changePassword(1L, request);

        assertEquals("encodedNewPassword", utilisateur.getMotDePasse());
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }
}
