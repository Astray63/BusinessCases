package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.ChargingStationDto;
import com.electriccharge.app.service.ChargingStationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bornes")
public class BorneController {

    @Autowired
    private ChargingStationService chargingStationService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> creerBorne(@Valid @RequestBody ChargingStationDto chargingStationDto) {
        try {
            ChargingStationDto nouvelleBorne = chargingStationService.create(chargingStationDto);
            return new ResponseEntity<>(ApiResponse.success("Borne créée avec succès", nouvelleBorne),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBorneById(@PathVariable Long id) {
        try {
            ChargingStationDto borne = chargingStationService.getById(id);
            return new ResponseEntity<>(ApiResponse.success(borne),
                    HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBornes() {
        try {
            List<ChargingStationDto> bornes = chargingStationService.getAll();
            return new ResponseEntity<>(ApiResponse.success(bornes),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBorne(@PathVariable Long id,
                                                    @Valid @RequestBody ChargingStationDto chargingStationDto) {
        try {
            ChargingStationDto updatedBorne = chargingStationService.update(id, chargingStationDto);
            return new ResponseEntity<>(ApiResponse.success("Borne mise à jour avec succès", updatedBorne),
                    HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBorne(@PathVariable Long id) {
        try {
            chargingStationService.delete(id);
            return new ResponseEntity<>(ApiResponse.success("Borne supprimée avec succès"),
                    HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lieu/{idLieu}")
    public ResponseEntity<ApiResponse<?>> getBornesByLieu(@PathVariable Long idLieu) {
        try {
            List<ChargingStationDto> bornes = chargingStationService.getByLieu(idLieu);
            return new ResponseEntity<>(ApiResponse.success(bornes),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/disponible/{disponible}")
    public ResponseEntity<ApiResponse<?>> getBornesByDisponibilite(@PathVariable Boolean disponible) {
        try {
            List<ChargingStationDto> bornes = chargingStationService.getByDisponibilite(disponible);
            return new ResponseEntity<>(ApiResponse.success(bornes),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/etat/{etat}")
    public ResponseEntity<ApiResponse<?>> getBornesByEtat(@PathVariable String etat) {
        try {
            List<ChargingStationDto> bornes = chargingStationService.getByEtat(etat);
            return new ResponseEntity<>(ApiResponse.success(bornes),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/public/proches")
    public ResponseEntity<ApiResponse<?>> getBornesProches(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double distance) {
        try {
            List<ChargingStationDto> bornes = chargingStationService.getProches(latitude, longitude, distance);
            return new ResponseEntity<>(ApiResponse.success(bornes),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/occupation")
    public ResponseEntity<ApiResponse<?>> toggleOccupation(
            @PathVariable Long id,
            @RequestParam Boolean occupee) {
        try {
            ChargingStationDto updatedBorne = chargingStationService.toggleOccupation(id, occupee);
            return new ResponseEntity<>(ApiResponse.success(updatedBorne),
                    HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/etat")
    public ResponseEntity<ApiResponse<?>> changerEtat(
            @PathVariable Long id,
            @RequestParam String nouvelEtat) {
        try {
            ChargingStationDto updatedBorne = chargingStationService.changerEtat(id, nouvelEtat);
            return new ResponseEntity<>(ApiResponse.success(updatedBorne),
                    HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
