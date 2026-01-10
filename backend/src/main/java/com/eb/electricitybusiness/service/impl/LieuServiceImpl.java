package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.LieuDto;
import com.eb.electricitybusiness.exception.ResourceNotFoundException;
import com.eb.electricitybusiness.model.Lieu;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.model.UtilisateurLieu;
import com.eb.electricitybusiness.repository.LieuRepository;
import com.eb.electricitybusiness.repository.UtilisateurLieuRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.LieuService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LieuServiceImpl implements LieuService {

    @Autowired
    private LieuRepository lieuRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private UtilisateurLieuRepository utilisateurLieuRepository;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public LieuDto create(LieuDto dto, Long userId) {
        // Vérifier que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        // Créer le lieu
        Lieu lieu = new Lieu();
        lieu.setNom(dto.nom());
        lieu.setAdresse(dto.adresse());
        lieu.setCodePostal(dto.codePostal());
        lieu.setVille(dto.ville());
        lieu.setPays(dto.pays() != null ? dto.pays() : "France");
        lieu.setLatitude(dto.latitude());
        lieu.setLongitude(dto.longitude());

        Lieu savedLieu = lieuRepository.save(lieu);

        // Créer la relation utilisateur-lieu
        UtilisateurLieu utilisateurLieu = new UtilisateurLieu();
        utilisateurLieu.setUtilisateur(utilisateur);
        utilisateurLieu.setLieu(savedLieu);
        utilisateurLieu.setTypeAdresse(UtilisateurLieu.TypeAdresse.principale);
        utilisateurLieuRepository.save(utilisateurLieu);

        return mapToDto(savedLieu);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public LieuDto update(Long id, LieuDto dto) {
        Lieu lieu = lieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lieu non trouvé avec l'ID: " + id));

        lieu.setNom(dto.nom());
        lieu.setAdresse(dto.adresse());
        lieu.setCodePostal(dto.codePostal());
        lieu.setVille(dto.ville());
        lieu.setPays(dto.pays());
        lieu.setLatitude(dto.latitude());
        lieu.setLongitude(dto.longitude());

        Lieu updatedLieu = lieuRepository.save(lieu);
        return mapToDto(updatedLieu);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void delete(Long id) {
        Lieu lieu = lieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lieu non trouvé avec l'ID: " + id));

        // Supprimer d'abord les relations utilisateur-lieu
        List<UtilisateurLieu> relations = utilisateurLieuRepository.findByLieu_IdLieu(id);
        utilisateurLieuRepository.deleteAll(relations);

        // Ensuite supprimer le lieu
        lieuRepository.delete(lieu);
    }

    @Override
    @SuppressWarnings("null")
    public LieuDto getById(Long id) {
        Lieu lieu = lieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lieu non trouvé avec l'ID: " + id));
        return mapToDto(lieu);
    }

    @Override
    public List<LieuDto> getAll() {
        return lieuRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LieuDto> getByUtilisateur(Long userId) {
        List<Lieu> lieux = utilisateurLieuRepository.findLieuxByUtilisateurId(userId);
        return lieux.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LieuDto> searchByNom(String nom) {
        return lieuRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LieuDto> getProches(Double latitude, Double longitude, Double distance) {
        // Convertir km en mètres pour ST_DWithin
        Double distanceMeters = distance * 1000;
        return lieuRepository.findByDistance(latitude, longitude, distanceMeters).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private LieuDto mapToDto(Lieu lieu) {
        return new LieuDto(
                lieu.getIdLieu(),
                lieu.getNom(),
                lieu.getAdresse(),
                lieu.getCodePostal(),
                lieu.getVille(),
                lieu.getPays(),
                lieu.getLatitude(),
                lieu.getLongitude(),
                lieu.getCreatedAt(),
                lieu.getUpdatedAt());
    }
}
