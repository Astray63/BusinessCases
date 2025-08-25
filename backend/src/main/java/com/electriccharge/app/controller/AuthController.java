package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.AuthRequestDto;
import com.electriccharge.app.dto.RegisterRequestDto;
import com.electriccharge.app.dto.UtilisateurDto;
import com.electriccharge.app.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.eb.electricitybusiness.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(
        UtilisateurService utilisateurService,
        AuthenticationManager authenticationManager,
        JwtUtils jwtUtils
    ) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }


@PostMapping("/register")
public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequestDto request) {
    try {
        UtilisateurDto nouveauUtilisateur = utilisateurService.creerUtilisateur(
            request.getUtilisateur(), 
            request.getMotDePasse()
        );
        return new ResponseEntity<>(ApiResponse.success("Inscription réussie", nouveauUtilisateur),
                HttpStatus.CREATED);
    } catch (Exception e) {
        return new ResponseEntity<>(ApiResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthRequestDto authRequest) {
        try {
            // Validate and clean input
            if (authRequest.pseudo() == null || authRequest.password() == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Le pseudo et le mot de passe sont requis", null));
            }
            String pseudo = authRequest.pseudo().trim();
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(pseudo, authRequest.password())
            );
            
            // Generate tokens
            String accessToken = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(pseudo);
            
            // Get user details
            UtilisateurDto utilisateur = utilisateurService.getUtilisateurByPseudo(pseudo);
            
            // Return successful response with tokens
            return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(new ApiResponse<>(
                    "SUCCESS",
                    "Connexion réussie",
                    Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "user", utilisateur
                    )
                ));
                
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("ERROR", "Identifiants invalides", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("ERROR", "Une erreur est survenue lors de la connexion: " + e.getMessage(), null));
        }
    }


    @PostMapping("/checkEmail")
    public ResponseEntity<ApiResponse<?>> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = utilisateurService.existsByEmail(email);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestParam String refreshToken) {
        try {
            if (!jwtUtils.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token invalide"));
            }

            String username = jwtUtils.getUsernameFromToken(refreshToken);
            String newAccessToken = jwtUtils.generateJwtToken(
                new UsernamePasswordAuthenticationToken(username, null)
            );
            
            return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newAccessToken)
                .body(ApiResponse.success("Token rafraîchi", Map.of(
                    "accessToken", newAccessToken
                )));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur lors du rafraîchissement du token: " + e.getMessage()));
        }
    }

    @PostMapping("/checkPseudo")
    public ResponseEntity<ApiResponse<?>> checkPseudoExists(@RequestBody String pseudo) {
        try {
            boolean exists = utilisateurService.existsByPseudo(pseudo);
            return new ResponseEntity<>(ApiResponse.success(exists), 
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage()), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
