package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.AuthRequestDto;
import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.dto.ChangePasswordRequestDto;
import com.electriccharge.app.exception.DuplicateResourceException;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.UtilisateurService;
import com.electriccharge.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Primary
public class UtilisateurServiceImpl implements UtilisateurService, UserDetailsService {

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException {
        return utilisateurRepository.findByPseudo(pseudo)
            .map(user -> {
                if (user.isEnabled()) {
                    return user;
                } else {
                    throw new UsernameNotFoundException("Compte non vérifié: " + pseudo);
                }
            })
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + pseudo));
    }

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${email.verification.code.expiry-minutes:15}")
    private int verificationCodeExpiryMinutes;

    @Override
    @Transactional
    public UtilisateurDto creerUtilisateur(UtilisateurDto utilisateurDto, String motDePasse) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(utilisateurDto.email())) {
            throw new DuplicateResourceException("Utilisateur", "email", utilisateurDto.email());
        }

        // Vérifier si le pseudo existe déjà
        if (utilisateurRepository.existsByPseudo(utilisateurDto.pseudo())) {
            throw new DuplicateResourceException("Utilisateur", "pseudo", utilisateurDto.pseudo());
        }

        // Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(utilisateurDto.nom());
        utilisateur.setPrenom(utilisateurDto.prenom());
        utilisateur.setPseudo(utilisateurDto.pseudo());
        utilisateur.setEmail(utilisateurDto.email());
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setRole(Utilisateur.Role.valueOf(utilisateurDto.role() != null ? utilisateurDto.role() : "client"));
        utilisateur.setDateNaissance(utilisateurDto.dateNaissance());
        utilisateur.setIban(utilisateurDto.iban());
        utilisateur.setAdressePhysique(utilisateurDto.adressePhysique());
        utilisateur.setMedias(utilisateurDto.medias() != null ? List.of(utilisateurDto.medias().split(",")) : List.of());
        utilisateur.setEstBanni(false);
        
        // Générer un code de vérification à 6 chiffres
        String verificationCode = generateVerificationCode();
        utilisateur.setVerificationCode(verificationCode);
        utilisateur.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        utilisateur.setEmailVerified(false);

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        
        // Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(
                savedUtilisateur.getEmail(), 
                savedUtilisateur.getPrenom() + " " + savedUtilisateur.getNom(),
                verificationCode
            );
        } catch (Exception e) {
            // Log l'erreur mais ne bloque pas l'inscription
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
        
        return mapToDto(savedUtilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToDto(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UtilisateurDto updateUtilisateur(Long id, UtilisateurDto utilisateurDto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        // Vérifier si l'email existe déjà pour un autre utilisateur
        if (!utilisateur.getEmail().equals(utilisateurDto.email()) 
                && utilisateurRepository.existsByEmail(utilisateurDto.email())) {
            throw new DuplicateResourceException("Utilisateur", "email", utilisateurDto.email());
        }

        // Vérifier si le pseudo existe déjà pour un autre utilisateur
        if (!utilisateur.getPseudo().equals(utilisateurDto.pseudo()) 
                && utilisateurRepository.existsByPseudo(utilisateurDto.pseudo())) {
            throw new DuplicateResourceException("Utilisateur", "pseudo", utilisateurDto.pseudo());
        }

        // Mettre à jour l'utilisateur
        utilisateur.setNom(utilisateurDto.nom());
        utilisateur.setPrenom(utilisateurDto.prenom());
        utilisateur.setPseudo(utilisateurDto.pseudo());
        utilisateur.setEmail(utilisateurDto.email());
        utilisateur.setDateNaissance(utilisateurDto.dateNaissance());
        utilisateur.setIban(utilisateurDto.iban());
        utilisateur.setAdressePhysique(utilisateurDto.adressePhysique());

        Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
        return mapToDto(updatedUtilisateur);
    }

    @Override
    @Transactional
    public void deleteUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto getUtilisateurByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
        return mapToDto(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto getUtilisateurByPseudo(String pseudo) {
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "pseudo", pseudo));
        return mapToDto(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> getUtilisateursWithVehicules() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validerMotDePasse(AuthRequestDto authRequestDto) {
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(authRequestDto.pseudo())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "pseudo", authRequestDto.pseudo()));
        
        return passwordEncoder.matches(authRequestDto.password(), utilisateur.getMotDePasse());
    }

    @Override
    @Transactional
    public void banirUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateur.setEstBanni(true);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public void reactiverUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateur.setEstBanni(false);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPseudo(String pseudo) {
        return utilisateurRepository.existsByPseudo(pseudo);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDto request) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }
        
        // Mettre à jour avec le nouveau mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Génère un code de vérification à 6 chiffres
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    @Transactional
    public boolean verifyEmail(String email, String code) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        // Vérifier si le compte est déjà vérifié
        if (utilisateur.getEmailVerified()) {
            throw new IllegalStateException("Ce compte est déjà vérifié");
        }

        // Vérifier si le code existe
        if (utilisateur.getVerificationCode() == null) {
            throw new IllegalStateException("Aucun code de vérification n'a été généré pour ce compte");
        }

        // Vérifier si le code a expiré
        if (utilisateur.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Le code de vérification a expiré. Veuillez en demander un nouveau.");
        }

        // Vérifier si le code correspond
        if (!utilisateur.getVerificationCode().equals(code)) {
            return false;
        }

        // Marquer l'email comme vérifié
        utilisateur.setEmailVerified(true);
        utilisateur.setVerificationCode(null);
        utilisateur.setVerificationCodeExpiry(null);
        utilisateurRepository.save(utilisateur);

        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(
                utilisateur.getEmail(),
                utilisateur.getPrenom() + " " + utilisateur.getNom()
            );
        } catch (Exception e) {
            // Log l'erreur mais ne bloque pas la vérification
            System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
        }

        return true;
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        // Vérifier si le compte est déjà vérifié
        if (utilisateur.getEmailVerified()) {
            throw new IllegalStateException("Ce compte est déjà vérifié");
        }

        // Générer un nouveau code
        String newCode = generateVerificationCode();
        utilisateur.setVerificationCode(newCode);
        utilisateur.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        utilisateurRepository.save(utilisateur);

        // Envoyer le nouvel email
        try {
            emailService.sendVerificationEmail(
                utilisateur.getEmail(),
                utilisateur.getPrenom() + " " + utilisateur.getNom(),
                newCode
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }

    private UtilisateurDto mapToDto(Utilisateur utilisateur) {
        return new UtilisateurDto(
            utilisateur.getIdUtilisateur(),
            utilisateur.getRole() != null ? utilisateur.getRole().getValue() : "client", // role
            utilisateur.getNom(),
            utilisateur.getPrenom(),
            utilisateur.getPseudo(),
            utilisateur.getEmail(),
            utilisateur.getDateNaissance(),
            utilisateur.getIban(),
            utilisateur.getAdressePhysique(),
            utilisateur.getMedias() != null && !utilisateur.getMedias().isEmpty() ? 
                String.join(",", utilisateur.getMedias()) : "", // medias
            null, // idAdresse - à ajuster selon votre logique métier
            !utilisateur.getEstBanni(), // actif (inverse de estBanni)
            utilisateur.getCreatedAt(),
            null // dateModification - pas dans le modèle actuel
        );
    }
}
