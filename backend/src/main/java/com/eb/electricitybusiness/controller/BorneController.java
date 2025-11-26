package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.service.ChargingStationService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/bornes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BorneController {

    @Autowired
    private ChargingStationService chargingStationService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> creerBorne(@Valid @RequestBody ChargingStationDto chargingStationDto) {
        ChargingStationDto nouvelleBorne = chargingStationService.create(chargingStationDto);
        return new ResponseEntity<>(ApiResponse.success("Borne créée avec succès", nouvelleBorne),
                HttpStatus.CREATED);
    }

    @GetMapping("/proches")
    public ResponseEntity<ApiResponse<?>> getBornesProches(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double distance) {
        List<ChargingStationDto> bornes = chargingStationService.getProches(latitude, longitude, distance);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBorneById(@PathVariable Long id) {
        ChargingStationDto borne = chargingStationService.getById(id);
        return new ResponseEntity<>(ApiResponse.success(borne),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBornes() {
        List<ChargingStationDto> bornes = chargingStationService.getAll();
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBorne(@PathVariable Long id,
            @Valid @RequestBody ChargingStationDto chargingStationDto) {
        ChargingStationDto updatedBorne = chargingStationService.update(id, chargingStationDto);
        return new ResponseEntity<>(ApiResponse.success("Borne mise à jour avec succès", updatedBorne),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBorne(@PathVariable Long id) {
        chargingStationService.delete(id);
        return new ResponseEntity<>(ApiResponse.success("Borne supprimée avec succès"),
                HttpStatus.OK);
    }

    @GetMapping("/lieu/{idLieu}")
    public ResponseEntity<ApiResponse<?>> getBornesByLieu(@PathVariable Long idLieu) {
        List<ChargingStationDto> bornes = chargingStationService.getByLieu(idLieu);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/disponible/{disponible}")
    public ResponseEntity<ApiResponse<?>> getBornesByDisponibilite(@PathVariable Boolean disponible) {
        List<ChargingStationDto> bornes = chargingStationService.getByDisponibilite(disponible);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<?>> getBornesDisponibles(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) java.math.BigDecimal prixMin,
            @RequestParam(required = false) java.math.BigDecimal prixMax,
            @RequestParam(required = false) Integer puissanceMin,
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) Boolean disponible) {
        // Si aucun filtre n'est fourni, retourner toutes les bornes disponibles
        if (latitude == null && longitude == null && prixMin == null &&
                prixMax == null && puissanceMin == null && etat == null && disponible == null) {
            List<ChargingStationDto> bornes = chargingStationService.getByDisponibilite(true);
            return new ResponseEntity<>(ApiResponse.success(bornes), HttpStatus.OK);
        }

        // Sinon utiliser la recherche avancée
        List<ChargingStationDto> bornes = chargingStationService.searchAdvanced(
                latitude, longitude, distance, prixMin, prixMax, puissanceMin, etat, disponible);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/etat/{etat}")
    public ResponseEntity<ApiResponse<?>> getBornesByEtat(@PathVariable String etat) {
        List<ChargingStationDto> bornes = chargingStationService.getByEtat(etat);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @PutMapping("/{id}/occupation")
    public ResponseEntity<ApiResponse<?>> toggleOccupation(
            @PathVariable Long id,
            @RequestParam Boolean occupee) {
        ChargingStationDto updatedBorne = chargingStationService.toggleOccupation(id, occupee);
        return new ResponseEntity<>(ApiResponse.success(updatedBorne),
                HttpStatus.OK);
    }

    @PutMapping("/{id}/etat")
    public ResponseEntity<ApiResponse<?>> changerEtat(
            @PathVariable Long id,
            @RequestParam String nouvelEtat) {
        ChargingStationDto updatedBorne = chargingStationService.changerEtat(id, nouvelEtat);
        return new ResponseEntity<>(ApiResponse.success(updatedBorne),
                HttpStatus.OK);
    }

    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<ApiResponse<?>> getBornesByUtilisateur(@PathVariable Long userId) {
        List<ChargingStationDto> bornes = chargingStationService.getByOwner(userId);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<ApiResponse<?>> getBornesByProprietaire(@PathVariable Long proprietaireId) {
        List<ChargingStationDto> bornes = chargingStationService.getByOwner(proprietaireId);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchBornes(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) java.math.BigDecimal prixMin,
            @RequestParam(required = false) java.math.BigDecimal prixMax,
            @RequestParam(required = false) Integer puissanceMin,
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) Boolean disponible) {
        List<ChargingStationDto> bornes = chargingStationService.searchAdvanced(
                latitude, longitude, distance, prixMin, prixMax, puissanceMin, etat, disponible);
        return new ResponseEntity<>(ApiResponse.success(bornes),
                HttpStatus.OK);
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<ApiResponse<?>> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("photos") MultipartFile[] photos) throws Exception {
        List<String> photoUrls = chargingStationService.uploadPhotos(id, photos);
        return new ResponseEntity<>(ApiResponse.success("Photos uploadées avec succès", photoUrls),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<ApiResponse<?>> deletePhoto(
            @PathVariable Long id,
            @RequestParam String photoUrl) throws Exception {
        chargingStationService.deletePhoto(id, photoUrl);
        return new ResponseEntity<>(ApiResponse.success("Photo supprimée avec succès"),
                HttpStatus.OK);
    }
}
