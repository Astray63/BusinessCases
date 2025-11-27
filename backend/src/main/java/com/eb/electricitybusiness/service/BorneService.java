package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.dto.BorneDto;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface BorneService {
    BorneDto create(BorneDto dto);

    Borne createBorne(Borne borne, Long userId, Long lieuId);

    BorneDto update(Long id, BorneDto dto);

    Borne updateBorne(Long id, Borne borneDetails);

    void delete(Long id);

    Borne getBorneById(Long id);

    BorneDto getBorneDtoById(Long id);

    List<Borne> getAllBornes();

    List<BorneDto> getAllBornesDto();

    List<Borne> getBornesByOwner(Long ownerId);

    List<BorneDto> getBornesByOwnerDto(Long ownerId);

    List<BorneDto> getByLieu(Long idLieu);

    List<BorneDto> getByDisponibilite(Boolean disponible);

    List<BorneDto> getByEtat(String etat);

    List<BorneDto> getProches(Double latitude, Double longitude, Double distance);

    BorneDto toggleOccupation(Long id, Boolean occupee);

    BorneDto changerEtat(Long id, String nouvelEtat);

    List<BorneDto> searchAdvanced(Double latitude, Double longitude, Double distance,
            BigDecimal prixMin, BigDecimal prixMax,
            Integer puissanceMin, String etat, Boolean disponible);

    List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception;

    void deletePhoto(Long borneId, String photoUrl) throws Exception;
}