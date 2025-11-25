package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.UtilisateurDto;
import com.eb.electricitybusiness.dto.ChangePasswordRequestDto;
import com.eb.electricitybusiness.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> creerUtilisateur(@Valid @RequestBody UtilisateurDto utilisateurDto,
            @RequestParam String motDePasse) {
        try {
            UtilisateurDto nouveauUtilisateur = utilisateurService.creerUtilisateur(utilisateurDto, motDePasse);
            return new ResponseEntity<>(ApiResponse.success("Utilisateur créé avec succès", nouveauUtilisateur),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUtilisateurById(@PathVariable Long id) {
        UtilisateurDto utilisateur = utilisateurService.getUtilisateurById(id);
        return new ResponseEntity<>(ApiResponse.success(utilisateur), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUtilisateurs() {
        List<UtilisateurDto> utilisateurs = utilisateurService.getAllUtilisateurs();
        return new ResponseEntity<>(ApiResponse.success(utilisateurs), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUtilisateur(@PathVariable Long id,
            @Valid @RequestBody UtilisateurDto utilisateurDto) {
        UtilisateurDto updatedUtilisateur = utilisateurService.updateUtilisateur(id, utilisateurDto);
        return new ResponseEntity<>(ApiResponse.success("Utilisateur mis à jour avec succès", updatedUtilisateur),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
        return new ResponseEntity<>(ApiResponse.success("Utilisateur supprimé avec succès"),
                HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<?>> getUtilisateurByEmail(@PathVariable String email) {
        UtilisateurDto utilisateur = utilisateurService.getUtilisateurByEmail(email);
        return new ResponseEntity<>(ApiResponse.success(utilisateur), HttpStatus.OK);
    }

    @GetMapping("/pseudo/{pseudo}")
    public ResponseEntity<ApiResponse<?>> getUtilisateurByPseudo(@PathVariable String pseudo) {
        UtilisateurDto utilisateur = utilisateurService.getUtilisateurByPseudo(pseudo);
        return new ResponseEntity<>(ApiResponse.success(utilisateur), HttpStatus.OK);
    }

    @GetMapping("/exists-by-email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(utilisateurService.existsByEmail(email));
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        try {
            utilisateurService.changePassword(id, request);
            return new ResponseEntity<>(ApiResponse.success("Mot de passe changé avec succès"),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
}