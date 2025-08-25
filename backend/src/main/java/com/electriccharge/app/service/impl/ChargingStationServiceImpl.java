package com.electriccharge.app.service.impl;

import com.electriccharge.app.dto.ChargingStationDto;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
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
        String etat = disponible ? "DISPONIBLE" : "OCCUPEE";
        List<ChargingStation> stations = chargingStationRepository.findByEtat(etat);
        return stations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        if (station.getOwner() != null) {
            dto.setOwnerId(station.getOwner().getIdUtilisateur());
        }
        return dto;
    }
}
