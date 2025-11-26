package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.model.ChargingStation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.ChargingStationRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.ChargingStationService;
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
        java.util.Objects.requireNonNull(id, "L'id ne peut pas être null");
        if (!chargingStationRepository.existsById(id)) {
            throw new EntityNotFoundException("Borne non trouvée avec l'id " + id);
        }
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        updateStationFromDto(station, dto);
        @SuppressWarnings("null")
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
        station.setDescription(dto.getDescription());

        Long ownerId = dto.getOwnerId();
        if (ownerId == null) {
            throw new IllegalArgumentException("L'ID du propriétaire ne peut pas être null");
        }
        Utilisateur owner = utilisateurRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id " + ownerId));
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
    @SuppressWarnings("null")
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
        List<ChargingStation> stations = chargingStationRepository.findByLieuxId(idLieu);
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

                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {

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
    @SuppressWarnings("null")
    public void delete(Long id) {
        // Vérifier si la borne existe
        if (!chargingStationRepository.existsById(id)) {
            throw new EntityNotFoundException("Borne non trouvée avec l'id " + id);
        }

        // Vérifier s'il y a des réservations actives
        if (reservationRepository.hasActiveReservations(id)) {
            throw new IllegalStateException(
                    "Impossible de supprimer la borne : des réservations actives existent pour cette borne");
        }

        Long idToDelete = id;
        chargingStationRepository.deleteById(idToDelete);
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
    @SuppressWarnings("null")
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
    @SuppressWarnings("null")
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

        // D'abord filtrer par distance si coordonnées fournies
        List<ChargingStation> stations;
        if (latitude != null && longitude != null && distance != null) {
            stations = chargingStationRepository.findByDistance(latitude, longitude, distance);
        } else {
            stations = chargingStationRepository.findAll();
        }

        // Appliquer les filtres supplémentaires
        List<ChargingStationDto> results = stations.stream()
                .filter(station -> matchesPriceMin(station, prixMin))
                .filter(station -> matchesPriceMax(station, prixMax))
                .filter(station -> matchesPowerMin(station, puissanceMin))
                .filter(station -> matchesState(station, etat))
                .filter(station -> matchesAvailability(station, disponible))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return results;
    }

    private boolean matchesPriceMin(ChargingStation station, java.math.BigDecimal prixMin) {
        if (prixMin != null && station.getPrixALaMinute() != null) {
            java.math.BigDecimal hourlyRate = station.getPrixALaMinute()
                    .multiply(java.math.BigDecimal.valueOf(60));
            return hourlyRate.compareTo(prixMin) >= 0;
        }
        return true;
    }

    private boolean matchesPriceMax(ChargingStation station, java.math.BigDecimal prixMax) {
        if (prixMax != null && station.getPrixALaMinute() != null) {
            java.math.BigDecimal hourlyRate = station.getPrixALaMinute()
                    .multiply(java.math.BigDecimal.valueOf(60));
            return hourlyRate.compareTo(prixMax) <= 0;
        }
        return true;
    }

    private boolean matchesPowerMin(ChargingStation station, Integer puissanceMin) {
        if (puissanceMin != null && station.getPuissance() != null) {
            return station.getPuissance() >= puissanceMin;
        }
        return true;
    }

    private boolean matchesState(ChargingStation station, String etat) {
        if (etat != null && station.getEtat() != null) {
            return station.getEtat().name().equalsIgnoreCase(etat);
        }
        return true;
    }

    private boolean matchesAvailability(ChargingStation station, Boolean disponible) {
        if (disponible != null && disponible) {
            return !Boolean.TRUE.equals(station.getOccupee());
        }
        return true;
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

            dto.setMedias(List.of());
        }

        dto.setInstructionSurPied(station.getInstructionSurPied());
        dto.setEtat(convertEtatToString(station.getEtat()));
        dto.setOccupee(station.getOccupee());
        dto.setPrixALaMinute(station.getPrixALaMinute());
        dto.setDescription(station.getDescription());

        // Calculate hourly rate
        if (station.getPrixALaMinute() != null) {
            dto.setHourlyRate(station.getPrixALaMinute().multiply(java.math.BigDecimal.valueOf(60)));
            dto.setPrix(dto.getHourlyRate()); // prix = hourlyRate
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

        }
        return dto;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception {

        ChargingStation station = chargingStationRepository.findById(borneId)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + borneId));

        List<String> photoUrls = new ArrayList<>();

        // Créer le répertoire d'upload s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Créer un sous-dossier pour cette borne
        Path borneUploadPath = uploadPath.resolve("borne-" + borneId);
        if (!Files.exists(borneUploadPath)) {
            Files.createDirectories(borneUploadPath);
        }

        // Limiter à 5 photos au total
        int currentPhotoCount = station.getMedias() != null ? station.getMedias().size() : 0;
        int maxPhotos = 5;
        int remainingSlots = maxPhotos - currentPhotoCount;

        if (remainingSlots <= 0) {
            throw new Exception("Limite de " + maxPhotos + " photos atteinte");
        }

        int photosToUpload = Math.min(photos.length, remainingSlots);

        for (int i = 0; i < photosToUpload; i++) {
            MultipartFile photo = photos[i];

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
    @SuppressWarnings("null")
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

            // On continue même si la suppression du fichier échoue
        }
    }
}
