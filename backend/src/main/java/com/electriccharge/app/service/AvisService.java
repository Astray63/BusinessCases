package com.electriccharge.app.service;

import com.electriccharge.app.dto.AvisDto;
import com.electriccharge.app.dto.CreateAvisDto;
import com.electriccharge.app.model.Avis;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.AvisRepository;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvisService {
    
    private final AvisRepository avisRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UtilisateurRepository utilisateurRepository;
    
    /**
     * Récupère tous les avis pour une borne
     */
    @Transactional(readOnly = true)
    public List<AvisDto> getAvisByChargingStation(Long chargingStationId) {
        log.info("Récupération des avis pour la borne {}", chargingStationId);
        
        List<Avis> avisList = avisRepository.findByChargingStationIdBorneOrderByCreatedAtDesc(chargingStationId);
        
        return avisList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les avis d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<AvisDto> getAvisByUser(Long userId) {
        log.info("Récupération des avis de l'utilisateur {}", userId);
        
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String pseudo = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            userId = utilisateur.getIdUtilisateur();
        }
        
        List<Avis> avisList = avisRepository.findByUtilisateurIdUtilisateurOrderByCreatedAtDesc(userId);
        
        return avisList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère la note moyenne d'une borne
     */
    @Transactional(readOnly = true)
    public Double getAverageNoteByChargingStation(Long chargingStationId) {
        Double average = avisRepository.getAverageNoteByChargingStation(chargingStationId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
    
    /**
     * Crée un nouvel avis
     */
    @Transactional
    public AvisDto createAvis(CreateAvisDto createAvisDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String pseudo = authentication.getName();
        
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Vérifier si l'utilisateur a déjà laissé un avis sur cette borne
        if (avisRepository.existsByUtilisateurIdUtilisateurAndChargingStationIdBorne(
                utilisateur.getIdUtilisateur(), createAvisDto.getChargingStationId())) {
            throw new RuntimeException("Vous avez déjà laissé un avis sur cette borne");
        }
        
        ChargingStation chargingStation = chargingStationRepository.findById(createAvisDto.getChargingStationId())
                .orElseThrow(() -> new RuntimeException("Borne non trouvée"));
        
        Avis avis = new Avis();
        avis.setNote(createAvisDto.getNote());
        avis.setCommentaire(createAvisDto.getCommentaire());
        avis.setUtilisateur(utilisateur);
        avis.setChargingStation(chargingStation);
        
        Avis savedAvis = avisRepository.save(avis);
        log.info("Avis créé avec succès pour la borne {} par l'utilisateur {}", 
                chargingStation.getNumero(), utilisateur.getPseudo());
        
        return convertToDto(savedAvis);
    }
    
    /**
     * Supprime un avis (seulement par son auteur)
     */
    @Transactional
    public void deleteAvis(Long avisId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String pseudo = authentication.getName();
        
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
        
        // Vérifier que l'utilisateur est bien l'auteur de l'avis
        if (!avis.getUtilisateur().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cet avis");
        }
        
        avisRepository.delete(avis);
        log.info("Avis {} supprimé avec succès", avisId);
    }
    
    /**
     * Convertit une entité Avis en DTO
     */
    private AvisDto convertToDto(Avis avis) {
        return AvisDto.builder()
                .idAvis(avis.getIdAvis())
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .createdAt(avis.getCreatedAt())
                .updatedAt(avis.getUpdatedAt())
                .utilisateurId(avis.getUtilisateur().getIdUtilisateur())
                .utilisateurPseudo(avis.getUtilisateur().getPseudo())
                .utilisateurNom(avis.getUtilisateur().getNom())
                .utilisateurPrenom(avis.getUtilisateur().getPrenom())
                .chargingStationId(avis.getChargingStation().getIdBorne())
                .chargingStationNom(avis.getChargingStation().getNom())
                .build();
    }
}
