package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.AuthRequestDto;
import com.eb.electricitybusiness.dto.UtilisateurDto;
import com.eb.electricitybusiness.dto.ChangePasswordRequestDto;
import com.eb.electricitybusiness.exception.DuplicateResourceException;
import com.eb.electricitybusiness.exception.ResourceNotFoundException;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.UtilisateurService;
import com.eb.electricitybusiness.service.EmailService;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UtilisateurServiceImpl.class);

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try to find by pseudo first, then by email
        Utilisateur user = utilisateurRepository.findByPseudo(identifier)
                .or(() -> utilisateurRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + identifier));

        if (user.isEnabled()) {
            return user;
        } else {
            throw new UsernameNotFoundException("Compte non vérifié: " + identifier);
        }
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

        // Générer un pseudo si non fourni
        String pseudo = utilisateurDto.pseudo();
        if (pseudo == null || pseudo.isBlank()) {
            pseudo = (utilisateurDto.prenom() + "." + utilisateurDto.nom()).toLowerCase();
            // Ensure uniqueness (simple logic for now, can be improved)
            int counter = 1;
            String originalPseudo = pseudo;
            while (utilisateurRepository.existsByPseudo(pseudo)) {
                pseudo = originalPseudo + counter++;
            }
        } else {
            // Vérifier si le pseudo existe déjà
            if (utilisateurRepository.existsByPseudo(utilisateurDto.pseudo())) {
                throw new DuplicateResourceException("Utilisateur", "pseudo", utilisateurDto.pseudo());
            }
        }

        // Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(utilisateurDto.nom());
        utilisateur.setPrenom(utilisateurDto.prenom());
        utilisateur.setPseudo(pseudo);
        utilisateur.setEmail(utilisateurDto.email());
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setRole(parseRole(utilisateurDto.role()));
        utilisateur.setDateNaissance(utilisateurDto.dateNaissance());
        utilisateur.setAdressePhysique(utilisateurDto.adressePhysique());
        utilisateur.setTelephone(utilisateurDto.telephone());
        utilisateur.setCodePostal(utilisateurDto.codePostal());
        utilisateur.setVille(utilisateurDto.ville());
        utilisateur.setIban(utilisateurDto.iban()); // Ajout du IBAN
        // utilisateur.setEstBanni(false); // Removed
        utilisateur.setEmailVerified(false);

        // Générer un code de vérification à 6 chiffres
        String verificationCode = generateVerificationCode();
        logger.info(">>> CODE DE VALIDATION POUR {}: {} <<<", utilisateurDto.email(), verificationCode);
        utilisateur.setVerificationCode(verificationCode);
        utilisateur.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        utilisateur.setEmailVerified(false);

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);

        // Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(
                    savedUtilisateur.getEmail(),
                    savedUtilisateur.getPrenom() + " " + savedUtilisateur.getNom(),
                    verificationCode);
        } catch (Exception e) {
            // Log l'erreur mais ne bloque pas l'inscription
            logger.error("Erreur lors de l'envoi de l'email de vérification", e);

        }

        return mapToDto(savedUtilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
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
    @SuppressWarnings("null")
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
        utilisateur.setAdressePhysique(utilisateurDto.adressePhysique());
        utilisateur.setTelephone(utilisateurDto.telephone());
        utilisateur.setCodePostal(utilisateurDto.codePostal());
        utilisateur.setVille(utilisateurDto.ville());
        utilisateur.setIban(utilisateurDto.iban());

        Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
        return mapToDto(updatedUtilisateur);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
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
    public boolean validerMotDePasse(AuthRequestDto authRequestDto) {
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(authRequestDto.pseudo())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "pseudo", authRequestDto.pseudo()));

        return passwordEncoder.matches(authRequestDto.password(), utilisateur.getMotDePasse());
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
    @SuppressWarnings("null")
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

        // Email de bienvenue supprimé sur demande
        // L'utilisateur est simplement redirigé vers le login après vérification

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
                    newCode);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }

    private UtilisateurDto mapToDto(Utilisateur utilisateur) {
        return new UtilisateurDto(
                utilisateur.getIdUtilisateur(),
                utilisateur.getRole().getValue(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getPseudo(),
                utilisateur.getEmail(),
                utilisateur.getDateNaissance(),
                utilisateur.getAdressePhysique(),
                utilisateur.getTelephone(),
                utilisateur.getCodePostal(),
                utilisateur.getVille(),
                utilisateur.getIban(),
                null, // idAdresse n'existe pas dans l'entité Utilisateur
                true, // actif
                utilisateur.getDateCreation(),
                utilisateur.getDateModification());
    }

    /**
     * Parse le rôle de manière tolérante (gère les variations de noms)
     */
    private Utilisateur.Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return Utilisateur.Role.client; // Valeur par défaut
        }

        // Normaliser : minuscules, trim
        String normalized = roleStr.toLowerCase().trim();

        switch (normalized) {
            case "client":
            case "user":
            case "utilisateur":
                return Utilisateur.Role.client;
            case "proprietaire":
            case "propriétaire":
            case "owner":
            case "pro":
                return Utilisateur.Role.proprietaire;
            default:
                try {
                    return Utilisateur.Role.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    return Utilisateur.Role.client; // Valeur par défaut en cas d'erreur
                }
        }
    }
}
