package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.AuthRequestDto;
import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.exception.DuplicateResourceException;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        utilisateur.setRole(Utilisateur.Role.valueOf(utilisateurDto.role()));
        utilisateur.setDateNaissance(utilisateurDto.dateNaissance());
        utilisateur.setIban(utilisateurDto.iban());
        utilisateur.setAdressePhysique(utilisateurDto.adressePhysique());
        utilisateur.setMedias(List.of(utilisateurDto.medias().split(",")));
        utilisateur.setEstBanni(false);
        utilisateur.setEmailVerified(true); // Temporarily set to true until email verification is implemented
        utilisateur.setVerificationCode(null);

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        return mapToDto(savedUtilisateur);
    }

    @Override
    public UtilisateurDto getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToDto(utilisateur);
    }

    @Override
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
        // Force initialization of lazy-loaded collections
        if (utilisateur.getMedias() != null) {
            utilisateur.getMedias().size();
        }
        return mapToDto(utilisateur);
    }

    @Override
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

    private UtilisateurDto mapToDto(Utilisateur utilisateur) {
        return new UtilisateurDto(
            utilisateur.getIdUtilisateur(),
            utilisateur.getRole().getValue(), // role
            utilisateur.getNom(),
            utilisateur.getPrenom(),
            utilisateur.getPseudo(),
            utilisateur.getEmail(),
            utilisateur.getDateNaissance(),
            utilisateur.getIban(),
            utilisateur.getAdressePhysique(),
            utilisateur.getMedias() != null && !utilisateur.getMedias().isEmpty() ? 
                String.join(",", utilisateur.getMedias()) : "", // medias
            null // idAdresse - à ajuster selon votre logique métier
        );
    }
}
