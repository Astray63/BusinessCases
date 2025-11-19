package com.electriccharge.app.service;

import com.electriccharge.app.dto.CreateSignalementDto;
import com.electriccharge.app.dto.SignalementDto;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Reservation;
import com.electriccharge.app.model.Signalement;
import com.electriccharge.app.model.Signalement.StatutSignalement;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.SignalementRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalementService {
    
    private final SignalementRepository signalementRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;
    
    /**
     * Récupère tous les signalements pour une borne
     */
    @Transactional(readOnly = true)
    public List<SignalementDto> getSignalementsByChargingStation(Long chargingStationId) {
        log.info("Récupération des signalements pour la borne {}", chargingStationId);
        
        List<Signalement> signalements = signalementRepository
                .findByChargingStationIdBorneOrderByDateSignalementDesc(chargingStationId);
        
        return signalements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les signalements d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<SignalementDto> getSignalementsByUser(Long userId) {
        log.info("Récupération des signalements de l'utilisateur {}", userId);
        
        List<Signalement> signalements = signalementRepository
                .findByUserIdUtilisateurOrderByDateSignalementDesc(userId);
        
        return signalements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les signalements par statut
     */
    @Transactional(readOnly = true)
    public List<SignalementDto> getSignalementsByStatut(StatutSignalement statut) {
        log.info("Récupération des signalements avec le statut {}", statut);
        
        List<Signalement> signalements = signalementRepository
                .findByStatutOrderByDateSignalementDesc(statut);
        
        return signalements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Crée un nouveau signalement
     */
    @Transactional
    public SignalementDto createSignalement(CreateSignalementDto createSignalementDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        ChargingStation chargingStation = chargingStationRepository
                .findById(createSignalementDto.getChargingStationId())
                .orElseThrow(() -> new RuntimeException("Borne non trouvée"));
        
        Signalement signalement = new Signalement();
        signalement.setDescription(createSignalementDto.getDescription());
        signalement.setUser(utilisateur);
        signalement.setChargingStation(chargingStation);
        signalement.setStatut(StatutSignalement.OUVERT);
        
        // Lier à une réservation si spécifié
        if (createSignalementDto.getReservationId() != null) {
            Reservation reservation = reservationRepository
                    .findById(createSignalementDto.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
            signalement.setReservation(reservation);
        }
        
        Signalement savedSignalement = signalementRepository.save(signalement);
        log.info("Signalement créé avec succès pour la borne {} par l'utilisateur {}", 
                chargingStation.getNumero(), utilisateur.getPseudo());
        
        return convertToDto(savedSignalement);
    }
    
    /**
     * Met à jour le statut d'un signalement
     */
    @Transactional
    public SignalementDto updateStatut(Long signalementId, StatutSignalement nouveauStatut) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
        
        signalement.setStatut(nouveauStatut);
        
        // Si résolu, on met la date de résolution
        if (nouveauStatut == StatutSignalement.RESOLU || nouveauStatut == StatutSignalement.FERME) {
            signalement.setDateResolution(LocalDateTime.now());
        }
        
        Signalement updatedSignalement = signalementRepository.save(signalement);
        log.info("Statut du signalement {} mis à jour vers {}", signalementId, nouveauStatut);
        
        return convertToDto(updatedSignalement);
    }
    
    /**
     * Supprime un signalement
     */
    @Transactional
    public void deleteSignalement(Long signalementId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
        
        // Vérifier que l'utilisateur est bien l'auteur du signalement
        if (!signalement.getUser().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce signalement");
        }
        
        signalementRepository.delete(signalement);
        log.info("Signalement {} supprimé avec succès", signalementId);
    }
    
    /**
     * Compte le nombre de signalements ouverts pour une borne
     */
    @Transactional(readOnly = true)
    public long countOpenSignalements(Long chargingStationId) {
        return signalementRepository.countByChargingStationIdBorneAndStatut(
                chargingStationId, StatutSignalement.OUVERT);
    }
    
    /**
     * Convertit une entité Signalement en DTO
     */
    private SignalementDto convertToDto(Signalement signalement) {
        SignalementDto.SignalementDtoBuilder builder = SignalementDto.builder()
                .idSignalement(signalement.getIdSignalement())
                .description(signalement.getDescription())
                .statut(signalement.getStatut())
                .dateSignalement(signalement.getDateSignalement())
                .dateResolution(signalement.getDateResolution())
                .createdAt(signalement.getCreatedAt())
                .updatedAt(signalement.getUpdatedAt())
                .userId(signalement.getUser().getIdUtilisateur())
                .userPseudo(signalement.getUser().getPseudo())
                .userNom(signalement.getUser().getNom())
                .userPrenom(signalement.getUser().getPrenom())
                .chargingStationId(signalement.getChargingStation().getIdBorne())
                .chargingStationNom(signalement.getChargingStation().getNom());
        
        if (signalement.getReservation() != null) {
            builder.reservationId(signalement.getReservation().getNumeroReservation());
        }
        
        return builder.build();
    }
}
