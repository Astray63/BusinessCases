package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody ReservationDto dto) {
        try {
            ReservationDto saved = reservationService.create(dto);
            return new ResponseEntity<>(ApiResponse.success("Réservation créée", saved), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(reservationService.getById(id)));
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<ApiResponse<?>> getByUser(@PathVariable Long userId) {
        List<ReservationDto> list = reservationService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/borne/{stationId}")
    public ResponseEntity<ApiResponse<?>> getByStation(@PathVariable Long stationId) {
        List<ReservationDto> list = reservationService.getByChargingStation(stationId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        List<ReservationDto> list = reservationService.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancel(@PathVariable Long id, @RequestParam Long requesterId) {
        try {
            ReservationDto dto = reservationService.cancel(id, requesterId);
            return ResponseEntity.ok(ApiResponse.success("Réservation annulée", dto));
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<?>> complete(@PathVariable Long id) {
        try {
            ReservationDto dto = reservationService.complete(id);
            return ResponseEntity.ok(ApiResponse.success("Réservation terminée", dto));
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
} 