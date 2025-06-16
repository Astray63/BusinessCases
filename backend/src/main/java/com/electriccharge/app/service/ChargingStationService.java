package com.electriccharge.app.service;

import com.electriccharge.app.dto.ChargingStationDto;
import java.util.List;

public interface ChargingStationService {
    
    ChargingStationDto create(ChargingStationDto chargingStationDto);
    
    ChargingStationDto getById(Long id);
    
    List<ChargingStationDto> getAll();
    
    ChargingStationDto update(Long id, ChargingStationDto chargingStationDto);
    
    void delete(Long id);
    
    List<ChargingStationDto> getByLieu(Long idLieu);
    
    List<ChargingStationDto> getByDisponibilite(Boolean disponible);
    
    List<ChargingStationDto> getByEtat(String etat);
    
    List<ChargingStationDto> getProches(Double latitude, Double longitude, Double distance);
    
    ChargingStationDto toggleOccupation(Long id, Boolean occupee);
    
    ChargingStationDto changerEtat(Long id, String nouvelEtat);
}