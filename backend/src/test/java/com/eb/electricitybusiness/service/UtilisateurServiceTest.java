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

    @Mock
    private com.eb.electricitybusiness.service.EmailService emailService;

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
                null, // IBAN
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
                null,
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

    @Test
    void loadUserByUsername_FoundAndEnabled_ReturnsUser() {
        Utilisateur user = new Utilisateur();
        user.setPseudo("user");
        user.setEmailVerified(true); // enabled

        when(utilisateurRepository.findByPseudo("user")).thenReturn(Optional.of(user));

        org.springframework.security.core.userdetails.UserDetails result = utilisateurService
                .loadUserByUsername("user");
        assertNotNull(result);
        assertEquals("user", result.getUsername());
    }

    @Test
    void loadUserByUsername_FoundButDisabled_ThrowsException() {
        Utilisateur user = new Utilisateur();
        user.setPseudo("user");
        user.setEmailVerified(false);

        when(utilisateurRepository.findByPseudo("user")).thenReturn(Optional.of(user));

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> utilisateurService.loadUserByUsername("user"));
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(utilisateurRepository.findByPseudo("user")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail("user")).thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> utilisateurService.loadUserByUsername("user"));
    }

    @Test
    void creerUtilisateur_AutoGeneratePseudo_Success() {
        String motDePasse = "password123";
        UtilisateurDto dto = new UtilisateurDto(
                null, "client", "Doe", "John", null, "john@test.com",
                LocalDate.of(1990, 1, 1), "Address", "0123", "75000", "Paris",
                null,
                null, true, null, null);

        // First attempt "john.doe" exists, second "john.doe1" works
        when(utilisateurRepository.existsByPseudo("john.doe")).thenReturn(true);
        when(utilisateurRepository.existsByPseudo("john.doe1")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(motDePasse)).thenReturn("encoded");

        UtilisateurDto result = utilisateurService.creerUtilisateur(dto, motDePasse);

        assertEquals("john.doe1", result.pseudo());
        verify(emailService).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void updateUtilisateur_ValidData_ReturnsUpdatedUser() {
        Long id = 1L;
        Utilisateur existingUser = new Utilisateur();
        existingUser.setIdUtilisateur(id);
        existingUser.setEmail("old@test.com");
        existingUser.setPseudo("oldPseudo");

        UtilisateurDto updateDto = new UtilisateurDto(
                id, "client", "New", "Name", "newPseudo", "new@test.com",
                LocalDate.now(), "New Addr", "0000", "00000", "City",
                null,
                null, true, null, null);

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(utilisateurRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(utilisateurRepository.existsByPseudo("newPseudo")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));

        UtilisateurDto result = utilisateurService.updateUtilisateur(id, updateDto);

        assertEquals("new@test.com", result.email());
        assertEquals("newPseudo", result.pseudo());
    }

    @Test
    void updateUtilisateur_DuplicateEmail_ThrowsException() {
        Long id = 1L;
        Utilisateur existingUser = new Utilisateur();
        existingUser.setIdUtilisateur(id);
        existingUser.setEmail("old@test.com");

        UtilisateurDto updateDto = new UtilisateurDto(
                id, "client", "New", "Name", "oldPseudo", "duplicate@test.com",
                LocalDate.now(), "New Addr", "0000", "00000", "City",
                null,
                null, true, null, null);

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(utilisateurRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> utilisateurService.updateUtilisateur(id, updateDto));
    }

    @Test
    void deleteUtilisateur_ValidId_DeletesUser() {
        Utilisateur user = new Utilisateur();
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));

        utilisateurService.deleteUtilisateur(1L);

        verify(utilisateurRepository).delete(user);
    }

    @Test
    void verifyEmail_Success() {
        String email = "test@test.com";
        String code = "123456";
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertTrue(utilisateurService.verifyEmail(email, code));
        assertTrue(user.getEmailVerified());
        assertNull(user.getVerificationCode());
    }

    @Test
    void verifyEmail_ExpiredCode_ThrowsException() {
        String email = "test@test.com";
        String code = "123456";
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().minusMinutes(1)); // Expired
        user.setEmailVerified(false);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> utilisateurService.verifyEmail(email, code));
    }

    @Test
    void verifyEmail_IncorrectCode_ReturnsFalse() {
        String email = "test@test.com";
        String code = "123456";
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertFalse(utilisateurService.verifyEmail(email, "WRONG"));
    }

    @Test
    void resendVerificationCode_Success() {
        String email = "test@test.com";
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setEmailVerified(false);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        utilisateurService.resendVerificationCode(email);

        assertNotNull(user.getVerificationCode());
        verify(emailService).sendVerificationEmail(eq(email), any(), any());
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsException() {
        Long id = 1L;
        Utilisateur user = new Utilisateur();
        user.setMotDePasse("encoded");

        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setAncienMotDePasse("wrong");

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> utilisateurService.changePassword(id, req));
    }

    @Test
    void getUtilisateurByEmail_Found_ReturnsDto() {
        Utilisateur user = new Utilisateur();
        user.setEmail("test@test.com");
        when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        assertNotNull(utilisateurService.getUtilisateurByEmail("test@test.com"));
    }
}
