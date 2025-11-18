package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.ChargingStationDto;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.ChargingStationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChargingStationServiceImpl implements ChargingStationService {

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Value("${app.upload.dir:${user.home}/electriccharge/uploads/bornes}")
    private String uploadDir;
    
    @Value("${app.upload.base-url:http://localhost:8080/uploads/bornes}")
    private String uploadBaseUrl;

    @Override
    @Transactional
    public ChargingStationDto create(ChargingStationDto dto) {
        ChargingStation station = new ChargingStation();
        updateStationFromDto(station, dto);
        ChargingStation savedStation = chargingStationRepository.save(station);
        return convertToDto(savedStation);
    }

    @Override
    @Transactional
    public ChargingStationDto update(Long id, ChargingStationDto dto) {
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        updateStationFromDto(station, dto);
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
    }

    private void updateStationFromDto(ChargingStation station, ChargingStationDto dto) {
        station.setNumero(dto.getNumero());
        station.setNom(dto.getNom());
        station.setLocalisation(dto.getLocalisation());
        station.setLatitude(dto.getLatitude());
        station.setLongitude(dto.getLongitude());
        station.setPuissance(dto.getPuissance());
        station.setMedias(dto.getMedias());
        station.setInstructionSurPied(dto.getInstructionSurPied());
        station.setEtat(parseEtat(dto.getEtat()));
        station.setOccupee(dto.getOccupee());
        station.setPrixALaMinute(dto.getPrixALaMinute());
        station.setConnectorType(dto.getConnectorType());
        station.setDescription(dto.getDescription());
        station.setAddress(dto.getAddress());
        station.setHourlyRate(dto.getHourlyRate());
        
        Utilisateur owner = utilisateurRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id " + dto.getOwnerId()));
        station.setOwner(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getAll() {
        List<ChargingStation> stations = chargingStationRepository.findAll();
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingStationDto getById(Long id) {
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        return convertToDto(station);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByOwner(Long ownerId) {
        List<ChargingStation> stations = chargingStationRepository.findByOwner_IdUtilisateur(ownerId);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByLieu(Long idLieu) {
        List<ChargingStation> stations = chargingStationRepository.findByChargingStationLieu_Lieu_IdLieu(idLieu);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByDisponibilite(Boolean disponible) {
        try {
            ChargingStation.Etat etat = disponible ? ChargingStation.Etat.DISPONIBLE : ChargingStation.Etat.OCCUPEE;
            List<ChargingStation> stations = chargingStationRepository.findByEtat(etat);
            return stations.stream()
                    .map(station -> {
                        try {
                            return convertToDto(station);
                        } catch (Exception e) {
                            System.err.println("Error converting station " + station.getIdBorne() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getByDisponibilite: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving available charging stations: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByEtat(String etat) {
        ChargingStation.Etat etatEnum = parseEtat(etat);
        List<ChargingStation> stations = chargingStationRepository.findByEtat(etatEnum);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Vérifier si la borne existe
        chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        
        // Vérifier s'il y a des réservations actives
        if (reservationRepository.hasActiveReservations(id)) {
            throw new IllegalStateException("Impossible de supprimer la borne : des réservations actives existent pour cette borne");
        }
        
        chargingStationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getProches(Double latitude, Double longitude, Double distance) {
        List<ChargingStation> stations = chargingStationRepository.findByDistance(latitude, longitude, distance);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChargingStationDto toggleOccupation(Long id, Boolean occupee) {
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        station.setOccupee(occupee);
        station.setEtat(occupee ? ChargingStation.Etat.OCCUPEE : ChargingStation.Etat.DISPONIBLE);
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
    }

    @Override
    @Transactional
    public ChargingStationDto changerEtat(Long id, String nouvelEtat) {
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        station.setEtat(parseEtat(nouvelEtat));
        if (nouvelEtat.equals("OCCUPEE")) {
            station.setOccupee(true);
        } else if (nouvelEtat.equals("DISPONIBLE")) {
            station.setOccupee(false);
        }
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> searchAdvanced(Double latitude, Double longitude, Double distance,
                                                     java.math.BigDecimal prixMin, java.math.BigDecimal prixMax,
                                                     Integer puissanceMin, String etat, Boolean disponible) {
        System.out.println("=== SEARCH ADVANCED ===");
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("Distance: " + distance + " km");
        System.out.println("Prix min: " + prixMin);
        System.out.println("Prix max: " + prixMax);
        System.out.println("Puissance min: " + puissanceMin);
        System.out.println("État: " + etat);
        System.out.println("Disponible seulement: " + disponible);
        
        // D'abord filtrer par distance si coordonnées fournies
        List<ChargingStation> stations;
        if (latitude != null && longitude != null && distance != null) {
            System.out.println("Recherche par distance...");
            stations = chargingStationRepository.findByDistance(latitude, longitude, distance);
            System.out.println("Trouvé " + stations.size() + " bornes dans le rayon");
        } else {
            System.out.println("Recherche globale...");
            stations = chargingStationRepository.findAll();
            System.out.println("Total bornes: " + stations.size());
        }
        
        // Appliquer les filtres supplémentaires
        List<ChargingStationDto> results = stations.stream()
                .filter(s -> {
                    if (prixMin != null && s.getHourlyRate() != null) {
                        boolean matches = s.getHourlyRate().compareTo(prixMin) >= 0;
                        if (!matches) System.out.println("  Borne " + s.getIdBorne() + " exclue (prix " + s.getHourlyRate() + " < min " + prixMin + ")");
                        return matches;
                    }
                    return true;
                })
                .filter(s -> {
                    if (prixMax != null && s.getHourlyRate() != null) {
                        boolean matches = s.getHourlyRate().compareTo(prixMax) <= 0;
                        if (!matches) System.out.println("  Borne " + s.getIdBorne() + " exclue (prix " + s.getHourlyRate() + " > max " + prixMax + ")");
                        return matches;
                    }
                    return true;
                })
                .filter(s -> {
                    if (puissanceMin != null && s.getPuissance() != null) {
                        boolean matches = s.getPuissance() >= puissanceMin;
                        if (!matches) System.out.println("  Borne " + s.getIdBorne() + " exclue (puissance " + s.getPuissance() + " < min " + puissanceMin + ")");
                        return matches;
                    }
                    return true;
                })
                .filter(s -> {
                    if (etat != null && s.getEtat() != null) {
                        boolean matches = s.getEtat().name().equalsIgnoreCase(etat);
                        if (!matches) System.out.println("  Borne " + s.getIdBorne() + " exclue (état " + s.getEtat() + " != " + etat + ")");
                        return matches;
                    }
                    return true;
                })
                .filter(s -> {
                    if (disponible != null && disponible) {
                        // Si "disponible seulement" est coché, ne garder que les bornes non occupées
                        boolean matches = !Boolean.TRUE.equals(s.getOccupee());
                        if (!matches) System.out.println("  Borne " + s.getIdBorne() + " exclue (occupée)");
                        return matches;
                    }
                    return true;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Résultats après filtres: " + results.size() + " bornes");
        return results;
    }

    private ChargingStation.Etat parseEtat(String etatStr) {
        if (etatStr == null) {
            return ChargingStation.Etat.DISPONIBLE;
        }
        try {
            return ChargingStation.Etat.valueOf(etatStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("État invalide: " + etatStr);
        }
    }

    private String convertEtatToString(ChargingStation.Etat etat) {
        return etat != null ? etat.name() : null;
    }

    private ChargingStationDto convertToDto(ChargingStation station) {
        ChargingStationDto dto = new ChargingStationDto();
        dto.setId(station.getIdBorne());
        dto.setIdBorne(station.getIdBorne()); // Ajouter pour compatibilité frontend
        dto.setNumero(station.getNumero());
        dto.setNom(station.getNom());
        dto.setLocalisation(station.getLocalisation());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setPuissance(station.getPuissance());
        
        // Charger les medias de manière sûre (lazy loading)
        try {
            List<String> medias = station.getMedias();
            dto.setMedias(medias != null ? new ArrayList<>(medias) : List.of());
        } catch (Exception e) {
            System.err.println("Could not load medias for station " + station.getIdBorne() + ": " + e.getMessage());
            dto.setMedias(List.of());
        }
        
        dto.setInstructionSurPied(station.getInstructionSurPied());
        dto.setEtat(convertEtatToString(station.getEtat()));
        dto.setOccupee(station.getOccupee());
        dto.setPrixALaMinute(station.getPrixALaMinute());
        dto.setConnectorType(station.getConnectorType());
        dto.setDescription(station.getDescription());
        dto.setAddress(station.getAddress());
        dto.setHourlyRate(station.getHourlyRate());
        
        // Mapper pour le frontend
        if (station.getHourlyRate() != null) {
            dto.setPrix(station.getHourlyRate()); // prix = hourlyRate
        }
        
        // Déterminer le type basé sur la puissance
        if (station.getPuissance() != null) {
            dto.setType(station.getPuissance() >= 50 ? "RAPIDE" : "NORMALE");
        }
        
        // Safely handle lazy-loaded owner relationship
        try {
            if (station.getOwner() != null) {
                dto.setOwnerId(station.getOwner().getIdUtilisateur());
            }
        } catch (Exception e) {
            // Handle LazyInitializationException or other issues
            System.err.println("Could not load owner for station " + station.getIdBorne() + ": " + e.getMessage());
        }
        return dto;
    }
    
    @Override
    @Transactional
    public List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception {
        System.out.println("Service: Début upload photos pour borne " + borneId);
        System.out.println("Upload directory: " + uploadDir);
        System.out.println("Upload base URL: " + uploadBaseUrl);
        
        ChargingStation station = chargingStationRepository.findById(borneId)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + borneId));
        
        List<String> photoUrls = new ArrayList<>();
        
        // Créer le répertoire d'upload s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        System.out.println("Path absolu: " + uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            System.out.println("Création du répertoire d'upload...");
            Files.createDirectories(uploadPath);
        }
        
        // Créer un sous-dossier pour cette borne
        Path borneUploadPath = uploadPath.resolve("borne-" + borneId);
        if (!Files.exists(borneUploadPath)) {
            System.out.println("Création du répertoire pour la borne " + borneId);
            Files.createDirectories(borneUploadPath);
        }
        
        // Limiter à 5 photos au total
        int currentPhotoCount = station.getMedias() != null ? station.getMedias().size() : 0;
        int maxPhotos = 5;
        int remainingSlots = maxPhotos - currentPhotoCount;
        
        System.out.println("Photos actuelles: " + currentPhotoCount + ", slots restants: " + remainingSlots);
        
        if (remainingSlots <= 0) {
            throw new Exception("Limite de " + maxPhotos + " photos atteinte");
        }
        
        int photosToUpload = Math.min(photos.length, remainingSlots);
        
        for (int i = 0; i < photosToUpload; i++) {
            MultipartFile photo = photos[i];
            
            System.out.println("Traitement photo " + (i+1) + ": " + photo.getOriginalFilename());
            
            // Valider le type de fichier
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new Exception("Le fichier doit être une image");
            }
            
            // Valider la taille (max 5MB)
            if (photo.getSize() > 5 * 1024 * 1024) {
                throw new Exception("La taille maximale par image est de 5MB");
            }
            
            // Générer un nom de fichier unique
            String originalFilename = photo.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Sauvegarder le fichier
            Path filePath = borneUploadPath.resolve(uniqueFilename);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Générer l'URL accessible
            String photoUrl = uploadBaseUrl + "/borne-" + borneId + "/" + uniqueFilename;
            photoUrls.add(photoUrl);
        }
        
        // Ajouter les nouvelles URLs à la liste existante
        if (station.getMedias() == null) {
            station.setMedias(new ArrayList<>());
        }
        station.getMedias().addAll(photoUrls);
        
        chargingStationRepository.save(station);
        
        return photoUrls;
    }
    
    @Override
    @Transactional
    public void deletePhoto(Long borneId, String photoUrl) throws Exception {
        ChargingStation station = chargingStationRepository.findById(borneId)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + borneId));
        
        if (station.getMedias() == null || !station.getMedias().contains(photoUrl)) {
            throw new EntityNotFoundException("Photo non trouvée");
        }
        
        // Supprimer l'URL de la liste
        station.getMedias().remove(photoUrl);
        chargingStationRepository.save(station);
        
        // Essayer de supprimer le fichier physique
        try {
            String filename = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
            String borneFolder = "borne-" + borneId;
            Path filePath = Paths.get(uploadDir).resolve(borneFolder).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du fichier physique: " + e.getMessage());
            // On continue même si la suppression du fichier échoue
        }
    }
}
