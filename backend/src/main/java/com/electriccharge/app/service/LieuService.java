package com.electriccharge.app.service;

import com.electriccharge.app.dto.LieuDto;
import java.util.List;

public interface LieuService {
    LieuDto create(LieuDto dto, Long userId);
    LieuDto update(Long id, LieuDto dto);
    void delete(Long id);
    LieuDto getById(Long id);
    List<LieuDto> getAll();
    List<LieuDto> getByUtilisateur(Long userId);
    List<LieuDto> searchByNom(String nom);
    List<LieuDto> getProches(Double latitude, Double longitude, Double distance);
}
