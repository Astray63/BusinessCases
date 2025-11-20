package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.AuthRequestDto;
import com.eb.electricitybusiness.dto.RegisterRequestDto;
import com.eb.electricitybusiness.dto.UtilisateurDto;
import com.eb.electricitybusiness.dto.VerifyEmailRequestDto;
import com.eb.electricitybusiness.dto.ResendVerificationRequestDto;
import com.eb.electricitybusiness.service.UtilisateurService;
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

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        try {
            boolean verified = utilisateurService.verifyEmail(request.email(), request.code());
            if (verified) {
                return ResponseEntity.ok(ApiResponse.success(
                    "Email vérifié avec succès ! Vous pouvez maintenant vous connecter.", 
                    null
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Code de vérification incorrect"));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur lors de la vérification: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<?>> resendVerification(@Valid @RequestBody ResendVerificationRequestDto request) {
        try {
            utilisateurService.resendVerificationCode(request.email());
            return ResponseEntity.ok(ApiResponse.success(
                "Un nouveau code de vérification a été envoyé à votre adresse email.", 
                null
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur lors de l'envoi du code: " + e.getMessage()));
        }
    }
}
