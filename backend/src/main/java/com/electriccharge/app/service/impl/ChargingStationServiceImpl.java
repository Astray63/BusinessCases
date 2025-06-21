package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.ChargingStationDto;
import com.electriccharge.app.exception.ResourceNotFoundException;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
import com.electriccharge.app.service.ChargingStationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChargingStationServiceImpl implements ChargingStationService {

    private static final Logger logger = LoggerFactory.getLogger(ChargingStationServiceImpl.class);
    
    private final ChargingStationRepository chargingStationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ChargingStationServiceImpl(
        ChargingStationRepository chargingStationRepository,
        UtilisateurRepository utilisateurRepository
    ) {
        this.chargingStationRepository = chargingStationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional
    public ChargingStationDto create(ChargingStationDto dto) {
        logger.debug("Creating new charging station");
        ChargingStation station = new ChargingStation();
        updateStationFromDto(station, dto);
        ChargingStation savedStation = chargingStationRepository.save(station);
        return convertToDto(savedStation);
    }

    @Override
    @Transactional
    public ChargingStationDto update(Long id, ChargingStationDto dto) {
        logger.debug("Updating charging station with id {}", id);
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borne non trouvée avec l'id " + id));
        updateStationFromDto(station, dto);
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getAll() {
        logger.debug("Fetching all charging stations");
        return chargingStationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingStationDto getById(Long id) {
        logger.debug("Fetching charging station with id {}", id);
        return convertToDto(chargingStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borne non trouvée avec l'id " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByOwner(Long ownerId) {
        logger.debug("Fetching charging stations for owner {}", ownerId);
        return chargingStationRepository.findByOwner_IdUtilisateur(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByLieu(Long idLieu) {
        logger.debug("Fetching charging stations for location {}", idLieu);
        return chargingStationRepository.findByChargingStationLieu_Lieu_IdLieu(idLieu).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByDisponibilite(Boolean disponible) {
        String etat = disponible ? "DISPONIBLE" : "OCCUPEE";
        logger.debug("Fetching charging stations with availability {}", etat);
        return chargingStationRepository.findByEtat(etat).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getByEtat(String etat) {
        logger.debug("Fetching charging stations with state {}", etat);
        return chargingStationRepository.findByEtat(etat).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.debug("Deleting charging station with id {}", id);
        chargingStationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationDto> getProches(Double latitude, Double longitude, Double distance) {
        logger.debug("Fetching charging stations near ({},{}) within {} km", latitude, longitude, distance);
        return chargingStationRepository.findByDistance(latitude, longitude, distance).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChargingStationDto toggleOccupation(Long id, Boolean occupee) {
        logger.debug("Setting occupied status to {} for station {}", occupee, id);
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borne non trouvée avec l'id " + id));
        station.setOccupee(occupee);
        station.setEtat(occupee ? ChargingStation.Etat.OCCUPEE : ChargingStation.Etat.DISPONIBLE);
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
    }

    @Override
    @Transactional
    public ChargingStationDto changerEtat(Long id, String nouvelEtat) {
        logger.debug("Changing state to {} for station {}", nouvelEtat, id);
        ChargingStation station = chargingStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borne non trouvée avec l'id " + id));
        station.setEtat(parseEtat(nouvelEtat));
        if (nouvelEtat.equals("OCCUPEE")) {
            station.setOccupee(true);
        } else if (nouvelEtat.equals("DISPONIBLE")) {
            station.setOccupee(false);
        }
        ChargingStation updatedStation = chargingStationRepository.save(station);
        return convertToDto(updatedStation);
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
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id " + dto.getOwnerId()));
        station.setOwner(owner);
    }

    private ChargingStationDto convertToDto(ChargingStation station) {
        ChargingStationDto dto = new ChargingStationDto();
        dto.setId(station.getIdBorne());
        dto.setNumero(station.getNumero());
        dto.setNom(station.getNom());
        dto.setLocalisation(station.getLocalisation());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setPuissance(station.getPuissance());
        dto.setMedias(station.getMedias() != null ? station.getMedias() : List.of());
        dto.setInstructionSurPied(station.getInstructionSurPied());
        dto.setEtat(station.getEtat() != null ? station.getEtat().name() : null);
        dto.setOccupee(station.getOccupee());
        dto.setPrixALaMinute(station.getPrixALaMinute());
        dto.setConnectorType(station.getConnectorType());
        dto.setDescription(station.getDescription());
        dto.setAddress(station.getAddress());
        dto.setHourlyRate(station.getHourlyRate());
        if (station.getOwner() != null) {
            dto.setOwnerId(station.getOwner().getIdUtilisateur());
        }
        return dto;
    }
}
