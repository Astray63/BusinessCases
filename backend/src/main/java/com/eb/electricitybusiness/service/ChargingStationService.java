package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.ChargingStationDto;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    List<ChargingStationDto> searchAdvanced(Double latitude, Double longitude, Double distance, 
                                             BigDecimal prixMin, BigDecimal prixMax, 
                                             Integer puissanceMin, String etat, Boolean disponible);
    List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception;
    void deletePhoto(Long borneId, String photoUrl) throws Exception;
}