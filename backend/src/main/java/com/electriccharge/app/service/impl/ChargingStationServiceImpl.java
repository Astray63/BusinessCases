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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
            String etat = disponible ? "DISPONIBLE" : "OCCUPEE";
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
        List<ChargingStation> stations = chargingStationRepository.findByEtat(etat);
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
        // D'abord filtrer par distance si coordonnées fournies
        List<ChargingStation> stations;
        if (latitude != null && longitude != null && distance != null) {
            stations = chargingStationRepository.findByDistance(latitude, longitude, distance);
        } else {
            stations = chargingStationRepository.findAll();
        }
        
        // Appliquer les filtres supplémentaires
        return stations.stream()
                .filter(s -> prixMin == null || (s.getHourlyRate() != null && s.getHourlyRate().compareTo(prixMin) >= 0))
                .filter(s -> prixMax == null || (s.getHourlyRate() != null && s.getHourlyRate().compareTo(prixMax) <= 0))
                .filter(s -> puissanceMin == null || (s.getPuissance() != null && s.getPuissance() >= puissanceMin))
                .filter(s -> etat == null || (s.getEtat() != null && s.getEtat().name().equals(etat.toUpperCase())))
                .filter(s -> disponible == null || (disponible && !s.getOccupee()) || (!disponible && s.getOccupee()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        dto.setMedias(station.getMedias() != null ? station.getMedias() : List.of());
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
}
