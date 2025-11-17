package com.electriccharge.app.service;

import com.electriccharge.app.dto.ReservationDto;

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

    List<ReservationDto> getAll();
} 