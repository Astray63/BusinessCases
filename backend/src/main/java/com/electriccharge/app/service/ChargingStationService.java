package com.electriccharge.app.service;

import com.electriccharge.app.dto.ChargingStationDto;
import java.util.List;

public interface ChargingStationService {
    ChargingStationDto create(ChargingStationDto dto);
    ChargingStationDto update(Long id, ChargingStationDto dto);
    void delete(Long id);
    ChargingStationDto getById(Long id);
    List<ChargingStationDto> getAll();
    List<ChargingStationDto> getByOwner(Long ownerId);
    List<ChargingStationDto> getByLieu(Long idLieu);
    List<ChargingStationDto> getByDisponibilite(Boolean disponible);
    List<ChargingStationDto> getByEtat(String etat);
    List<ChargingStationDto> getProches(Double latitude, Double longitude, Double distance);
    ChargingStationDto toggleOccupation(Long id, Boolean occupee);
    ChargingStationDto changerEtat(Long id, String nouvelEtat);
}