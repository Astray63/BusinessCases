package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Lieu;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.LieuRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.eb.electricitybusiness.service.BorneService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorneServiceImpl implements BorneService {

    private final BorneRepository borneRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;
    private final LieuRepository lieuRepository;

    @Value("${app.upload.dir:${user.home}/electriccharge/uploads/bornes}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads/bornes}")
    private String uploadBaseUrl;

    public BorneServiceImpl(BorneRepository borneRepository,
            UtilisateurRepository utilisateurRepository,
            ReservationRepository reservationRepository,
            LieuRepository lieuRepository) {
        this.borneRepository = borneRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.reservationRepository = reservationRepository;
        this.lieuRepository = lieuRepository;
    }

    @Override
    @Transactional
    public BorneDto create(BorneDto dto) {
        Borne borne = new Borne();
        updateBorneFromDto(borne, dto);
        Borne savedBorne = borneRepository.save(borne);
        return convertToDto(savedBorne);
    }

    @Override
    public Borne createBorne(Borne borne, Long userId, Long lieuId) {
        java.util.Objects.requireNonNull(userId, "userId ne peut pas être null");
        java.util.Objects.requireNonNull(lieuId, "lieuId ne peut pas être null");

        Utilisateur owner = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Lieu lieu = lieuRepository.findById(lieuId)
                .orElseThrow(() -> new RuntimeException("Lieu non trouvé"));

        borne.setOwner(owner);
        borne.setLieu(lieu);
        return borneRepository.save(borne);
    }

    @Override
    @Transactional
    public BorneDto update(Long id, BorneDto dto) {
        java.util.Objects.requireNonNull(id, "L'id ne peut pas être null");
        if (!borneRepository.existsById(id)) {
            throw new EntityNotFoundException("Borne non trouvée avec l'id " + id);
        }
        Borne borne = borneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        updateBorneFromDto(borne, dto);
        @SuppressWarnings("null")
        Borne updatedBorne = borneRepository.save(borne);
        return convertToDto(updatedBorne);
    }

    @Override
    public Borne updateBorne(Long id, Borne borneDetails) {
        java.util.Objects.requireNonNull(id, "L'id ne peut pas être null");
        Borne borne = getBorneById(id);

        borne.setNom(borneDetails.getNom());
        borne.setNumero(borneDetails.getNumero());
        borne.setLocalisation(borneDetails.getLocalisation());
        borne.setLatitude(borneDetails.getLatitude());
        borne.setLongitude(borneDetails.getLongitude());
        borne.setPuissance(borneDetails.getPuissance());
        borne.setPrixALaMinute(borneDetails.getPrixALaMinute());
        borne.setInstructionSurPied(borneDetails.getInstructionSurPied());
        borne.setDescription(borneDetails.getDescription());
        borne.setEtat(borneDetails.getEtat());

        return borneRepository.save(borne);
    }

    @Override
    public Borne getBorneById(Long id) {
        java.util.Objects.requireNonNull(id, "L'id ne peut pas être null");
        return borneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
    }

    @Override
    public BorneDto getBorneDtoById(Long id) {
        Borne borne = getBorneById(id);
        return convertToDto(borne);
    }

    @Override
    public List<Borne> getAllBornes() {
        return borneRepository.findAll();
    }

    @Override
    public List<BorneDto> getAllBornesDto() {
        return getAllBornes().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Borne> getBornesByOwner(Long ownerId) {
        return borneRepository.findByOwnerIdUtilisateur(ownerId);
    }

    @Override
    public List<BorneDto> getBornesByOwnerDto(Long ownerId) {
        return getBornesByOwner(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorneDto> getByLieu(Long idLieu) {
        List<Borne> stations = borneRepository.findByLieuxId(idLieu);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void updateBorneFromDto(Borne borne, BorneDto dto) {
        borne.setNumero(dto.getNumero());
        borne.setNom(dto.getNom());
        borne.setLocalisation(dto.getLocalisation());
        borne.setLatitude(dto.getLatitude());
        borne.setLongitude(dto.getLongitude());
        borne.setPuissance(dto.getPuissance());
        borne.setMedias(dto.getMedias());
        borne.setInstructionSurPied(dto.getInstructionSurPied());
        borne.setEtat(parseEtat(dto.getEtat()));
        borne.setOccupee(dto.getOccupee());
        borne.setPrixALaMinute(dto.getPrixALaMinute());
        borne.setDescription(dto.getDescription());

        Long ownerId = dto.getOwnerId();
        if (ownerId == null) {
            throw new IllegalArgumentException("L'ID du propriétaire ne peut pas être null");
        }
        Utilisateur owner = utilisateurRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id " + ownerId));
        borne.setOwner(owner);

        Long lieuId = dto.getLieuId();
        if (lieuId == null) {
            throw new IllegalArgumentException("L'ID du lieu ne peut pas être null");
        }
        Lieu lieu = lieuRepository.findById(lieuId)
                .orElseThrow(() -> new EntityNotFoundException("Lieu non trouvé avec l'id " + lieuId));
        borne.setLieu(lieu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorneDto> getByDisponibilite(Boolean disponible) {
        try {
            Borne.Etat etat = disponible ? Borne.Etat.DISPONIBLE : Borne.Etat.OCCUPEE;
            List<Borne> stations = borneRepository.findByEtat(etat);
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
    public List<BorneDto> getByEtat(String etat) {
        Borne.Etat etatEnum = parseEtat(etat);
        List<Borne> stations = borneRepository.findByEtat(etatEnum);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void delete(Long id) {
        // Vérifier si la borne existe
        if (!borneRepository.existsById(id)) {
            throw new EntityNotFoundException("Borne non trouvée avec l'id " + id);
        }

        // Vérifier s'il y a des réservations actives
        if (reservationRepository.hasActiveReservations(id)) {
            throw new IllegalStateException(
                    "Impossible de supprimer la borne : des réservations actives existent pour cette borne");
        }

        borneRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorneDto> getProches(Double latitude, Double longitude, Double distance) {
        List<Borne> stations = borneRepository.findByDistance(latitude, longitude, distance);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public BorneDto toggleOccupation(Long id, Boolean occupee) {
        Borne station = borneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        station.setOccupee(occupee);
        station.setEtat(occupee ? Borne.Etat.OCCUPEE : Borne.Etat.DISPONIBLE);
        Borne updatedStation = borneRepository.save(station);
        return convertToDto(updatedStation);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public BorneDto changerEtat(Long id, String nouvelEtat) {
        Borne station = borneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + id));
        station.setEtat(parseEtat(nouvelEtat));
        if (nouvelEtat.equals("OCCUPEE")) {
            station.setOccupee(true);
        } else if (nouvelEtat.equals("DISPONIBLE")) {
            station.setOccupee(false);
        }
        Borne updatedStation = borneRepository.save(station);
        return convertToDto(updatedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorneDto> searchAdvanced(Double latitude, Double longitude, Double distance,
            java.math.BigDecimal prixMin, java.math.BigDecimal prixMax,
            Integer puissanceMin, String etat, Boolean disponible) {

        // D'abord filtrer par distance si coordonnées fournies
        List<Borne> stations;
        if (latitude != null && longitude != null && distance != null) {
            stations = borneRepository.findByDistance(latitude, longitude, distance);
        } else {
            stations = borneRepository.findAll();
        }

        // Appliquer les filtres supplémentaires
        List<BorneDto> results = stations.stream()
                .filter(station -> matchesPriceMin(station, prixMin))
                .filter(station -> matchesPriceMax(station, prixMax))
                .filter(station -> matchesPowerMin(station, puissanceMin))
                .filter(station -> matchesState(station, etat))
                .filter(station -> matchesAvailability(station, disponible))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return results;
    }

    private boolean matchesPriceMin(Borne station, java.math.BigDecimal prixMin) {
        if (prixMin != null && station.getPrixALaMinute() != null) {
            java.math.BigDecimal hourlyRate = station.getPrixALaMinute()
                    .multiply(java.math.BigDecimal.valueOf(60));
            return hourlyRate.compareTo(prixMin) >= 0;
        }
        return true;
    }

    private boolean matchesPriceMax(Borne station, java.math.BigDecimal prixMax) {
        if (prixMax != null && station.getPrixALaMinute() != null) {
            java.math.BigDecimal hourlyRate = station.getPrixALaMinute()
                    .multiply(java.math.BigDecimal.valueOf(60));
            return hourlyRate.compareTo(prixMax) <= 0;
        }
        return true;
    }

    private boolean matchesPowerMin(Borne station, Integer puissanceMin) {
        if (puissanceMin != null && station.getPuissance() != null) {
            return station.getPuissance() >= puissanceMin;
        }
        return true;
    }

    private boolean matchesState(Borne station, String etat) {
        if (etat != null && station.getEtat() != null) {
            return station.getEtat().name().equalsIgnoreCase(etat);
        }
        return true;
    }

    private boolean matchesAvailability(Borne station, Boolean disponible) {
        if (disponible != null && disponible) {
            return !Boolean.TRUE.equals(station.getOccupee());
        }
        return true;
    }

    private Borne.Etat parseEtat(String etatStr) {
        if (etatStr == null) {
            return Borne.Etat.DISPONIBLE;
        }

        // Normaliser la chaîne : supprimer les espaces et convertir en majuscules
        String normalizedEtat = etatStr.toUpperCase().trim().replace(" ", "_");

        // Gérer les alias courants du frontend
        switch (normalizedEtat) {
            case "MAINTENANCE":
            case "EN_MAINTENANCE":
                return Borne.Etat.EN_MAINTENANCE;
            case "PANNE":
            case "EN_PANNE":
                return Borne.Etat.EN_PANNE;
            case "DISPONIBLE":
                return Borne.Etat.DISPONIBLE;
            case "OCCUPEE":
            case "OCCUPÉE":
                return Borne.Etat.OCCUPEE;
            default:
                try {
                    return Borne.Etat.valueOf(normalizedEtat);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("État invalide: " + etatStr +
                            ". États valides: DISPONIBLE, OCCUPEE, EN_PANNE, EN_MAINTENANCE");
                }
        }
    }

    private String convertEtatToString(Borne.Etat etat) {
        return etat != null ? etat.name() : null;
    }

    private BorneDto convertToDto(Borne station) {
        BorneDto dto = new BorneDto();
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

        // Calculer le taux horaire
        if (station.getPrixALaMinute() != null) {
            dto.setHourlyRate(station.getPrixALaMinute().multiply(java.math.BigDecimal.valueOf(60)));
            dto.setPrix(dto.getHourlyRate()); // prix = taux horaire
        }

        // Déterminer le type basé sur la puissance
        if (station.getPuissance() != null) {
            dto.setType(station.getPuissance() >= 50 ? "RAPIDE" : "NORMALE");
        }

        // Gérer en toute sécurité la relation propriétaire chargée paresseusement
        try {
            if (station.getOwner() != null) {
                dto.setOwnerId(station.getOwner().getIdUtilisateur());
            }
            if (station.getLieu() != null) {
                dto.setLieuId(station.getLieu().getIdLieu());
            }
        } catch (Exception e) {
            // Gérer LazyInitializationException ou autres problèmes
        }
        return dto;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception {

        Borne station = borneRepository.findById(borneId)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + borneId));

        List<String> photoDataUrls = new ArrayList<>();

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

            // Valider la taille (max 2MB pour Base64 - plus petit car stocké en BDD)
            if (photo.getSize() > 2 * 1024 * 1024) {
                throw new Exception("La taille maximale par image est de 2MB");
            }

            // Convertir l'image en Base64 Data URL
            byte[] imageBytes = photo.getBytes();
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:" + contentType + ";base64," + base64Image;

            photoDataUrls.add(dataUrl);
        }

        // Ajouter les nouvelles Data URLs à la liste existante
        if (station.getMedias() == null) {
            station.setMedias(new ArrayList<>());
        }
        station.getMedias().addAll(photoDataUrls);

        borneRepository.save(station);

        return photoDataUrls;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deletePhoto(Long borneId, String photoUrl) throws Exception {
        Borne station = borneRepository.findById(borneId)
                .orElseThrow(() -> new EntityNotFoundException("Borne non trouvée avec l'id " + borneId));

        if (station.getMedias() == null || !station.getMedias().contains(photoUrl)) {
            throw new EntityNotFoundException("Photo non trouvée");
        }

        // Supprimer l'URL/Data URL de la liste
        station.getMedias().remove(photoUrl);
        borneRepository.save(station);
    }
}
