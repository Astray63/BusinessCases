package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.LieuDto;
import com.eb.electricitybusiness.service.LieuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lieux")
public class LieuController {

    @Autowired
    private LieuService lieuService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> creerLieu(
            @Valid @RequestBody LieuDto lieuDto,
            @RequestParam Long userId) {
        try {
            LieuDto nouveauLieu = lieuService.create(lieuDto, userId);
            return new ResponseEntity<>(
                ApiResponse.success("Lieu créé avec succès", nouveauLieu),
                HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getLieuById(@PathVariable Long id) {
        try {
            LieuDto lieu = lieuService.getById(id);
            return new ResponseEntity<>(
                ApiResponse.success(lieu),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllLieux() {
        try {
            List<LieuDto> lieux = lieuService.getAll();
            return new ResponseEntity<>(
                ApiResponse.success(lieux),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<ApiResponse<?>> getLieuxByUtilisateur(@PathVariable Long userId) {
        try {
            List<LieuDto> lieux = lieuService.getByUtilisateur(userId);
            return new ResponseEntity<>(
                ApiResponse.success(lieux),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchLieux(@RequestParam String nom) {
        try {
            List<LieuDto> lieux = lieuService.searchByNom(nom);
            return new ResponseEntity<>(
                ApiResponse.success(lieux),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/proches")
    public ResponseEntity<ApiResponse<?>> getLieuxProches(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double distance) {
        try {
            List<LieuDto> lieux = lieuService.getProches(latitude, longitude, distance);
            return new ResponseEntity<>(
                ApiResponse.success(lieux),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateLieu(
            @PathVariable Long id,
            @Valid @RequestBody LieuDto lieuDto) {
        try {
            LieuDto updatedLieu = lieuService.update(id, lieuDto);
            return new ResponseEntity<>(
                ApiResponse.success("Lieu mis à jour avec succès", updatedLieu),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteLieu(@PathVariable Long id) {
        try {
            lieuService.delete(id);
            return new ResponseEntity<>(
                ApiResponse.success("Lieu supprimé avec succès"),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }
}
