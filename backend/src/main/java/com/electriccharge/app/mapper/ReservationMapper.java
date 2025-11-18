package com.electriccharge.app.mapper;

import com.electriccharge.app.dto.BorneDto;
import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.dto.UtilisateurSimpleDto;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Reservation;
import com.electriccharge.app.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Reservation entities and DTOs
 */
@Component
public class ReservationMapper {

    private static final Logger logger = LoggerFactory.getLogger(ReservationMapper.class);

    /**
     * Converts a Reservation entity to a ReservationDto
     */
    public ReservationDto toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getNumeroReservation());
        dto.setUtilisateurId(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getIdUtilisateur() : null);
        dto.setChargingStationId(reservation.getChargingStation() != null ? reservation.getChargingStation().getIdBorne() : null);
        dto.setDateDebut(reservation.getDateDebut());
        dto.setDateFin(reservation.getDateFin());
        dto.setEtat(reservation.getEtat() != null ? reservation.getEtat().name() : null);
        dto.setPrixALaMinute(reservation.getPrixALaMinute());
        dto.setTotalPrice(reservation.getTotalPrice());
        dto.setReceiptPath(reservation.getReceiptPath());
        
        // Populate nested objects
        if (reservation.getChargingStation() != null) {
            dto.setBorne(toBorneDto(reservation.getChargingStation()));
        }
        
        if (reservation.getUtilisateur() != null) {
            dto.setUtilisateur(toUtilisateurSimpleDto(reservation.getUtilisateur()));
        }
        
        return dto;
    }

    /**
     * Converts a list of Reservation entities to DTOs
     */
    public List<ReservationDto> toDtoList(List<Reservation> reservations) {
        if (reservations == null) {
            return new ArrayList<>();
        }
        return reservations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a ChargingStation entity to a BorneDto
     */
    public BorneDto toBorneDto(ChargingStation station) {
        if (station == null) {
            return null;
        }

        BorneDto dto = new BorneDto();
        dto.setId(station.getIdBorne());
        dto.setNumero(station.getNumero());
        dto.setNom(station.getNom());
        dto.setLocalisation(station.getLocalisation());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setPrixALaMinute(station.getPrixALaMinute());
        dto.setPuissance(station.getPuissance());
        
        // Safely handle lazy loading of medias
        try {
            dto.setMedias(station.getMedias());
        } catch (Exception e) {
            logger.warn("Could not load medias for station {}", station.getIdBorne());
            dto.setMedias(new ArrayList<>());
        }
        
        dto.setInstructionSurPied(station.getInstructionSurPied());
        dto.setConnectorType(station.getConnectorType());
        dto.setDescription(station.getDescription());
        dto.setEtat(station.getEtat() != null ? station.getEtat().name() : null);
        dto.setOccupee(station.getOccupee());
        
        // Safely handle lazy loading of owner
        try {
            if (station.getOwner() != null) {
                dto.setOwnerId(station.getOwner().getIdUtilisateur());
            }
        } catch (Exception e) {
            logger.warn("Could not load owner for station {}", station.getIdBorne());
        }
        
        return dto;
    }

    /**
     * Converts a Utilisateur entity to a UtilisateurSimpleDto
     */
    public UtilisateurSimpleDto toUtilisateurSimpleDto(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setIdUtilisateur(utilisateur.getIdUtilisateur());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setPseudo(utilisateur.getPseudo());
        dto.setEmail(utilisateur.getEmail());
        dto.setDateNaissance(utilisateur.getDateNaissance());
        dto.setRole(utilisateur.getRole() != null ? utilisateur.getRole().name() : null);
        dto.setIban(utilisateur.getIban());
        dto.setAdressePhysique(utilisateur.getAdressePhysique());
        
        // Convert List<String> to String (comma-separated)
        if (utilisateur.getMedias() != null && !utilisateur.getMedias().isEmpty()) {
            dto.setMedias(String.join(",", utilisateur.getMedias()));
        }
        
        dto.setAccountLocked(!utilisateur.isAccountNonLocked());
        return dto;
    }

    /**
     * Partially updates a Reservation entity from a DTO
     * Only updates non-null values
     */
    public void updateEntityFromDto(Reservation reservation, ReservationDto dto) {
        if (reservation == null || dto == null) {
            return;
        }

        if (dto.getDateDebut() != null) {
            reservation.setDateDebut(dto.getDateDebut());
        }

        if (dto.getDateFin() != null) {
            reservation.setDateFin(dto.getDateFin());
        }

        if (dto.getPrixALaMinute() != null) {
            reservation.setPrixALaMinute(dto.getPrixALaMinute());
        }

        if (dto.getTotalPrice() != null) {
            reservation.setTotalPrice(dto.getTotalPrice());
        }

        if (dto.getEtat() != null) {
            try {
                reservation.setEtat(Reservation.EtatReservation.valueOf(dto.getEtat()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid reservation state: {}", dto.getEtat());
            }
        }
    }
}
