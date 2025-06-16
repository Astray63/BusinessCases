package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.service.UtilisateurService;
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

    @PutMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<?>> banirUtilisateur(@PathVariable Long id) {
        utilisateurService.banirUtilisateur(id);
        return new ResponseEntity<>(ApiResponse.success("Utilisateur banni avec succès"), 
                HttpStatus.OK);
    }

    @PutMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<?>> reactiverUtilisateur(@PathVariable Long id) {
        utilisateurService.reactiverUtilisateur(id);
        return new ResponseEntity<>(ApiResponse.success("Utilisateur réactivé avec succès"), 
                HttpStatus.OK);
    }
}