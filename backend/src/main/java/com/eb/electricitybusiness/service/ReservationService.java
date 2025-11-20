package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.ReservationDto;

import java.util.List;

public interface ReservationService {
    ReservationDto create(ReservationDto dto);

    ReservationDto cancel(Long reservationId, Long requesterId);

    ReservationDto complete(Long reservationId);
    
    ReservationDto accepter(Long reservationId, Long proprietaireId);
    
    ReservationDto refuser(Long reservationId, Long proprietaireId, String motif);

    ReservationDto getById(Long id);

    List<ReservationDto> getByUser(Long userId);

    List<ReservationDto> getByChargingStation(Long stationId);
    
    List<ReservationDto> getByOwner(Long ownerId);

    List<ReservationDto> filtrer(String statut, java.time.LocalDateTime dateDebut, java.time.LocalDateTime dateFin, Long borneId, Long utilisateurId);

    List<ReservationDto> getAll();
} 