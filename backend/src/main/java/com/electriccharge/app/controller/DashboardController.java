package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ApiResponse;
import com.electriccharge.app.dto.DashboardStatsDto;
import com.electriccharge.app.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Récupère les statistiques du dashboard pour l'utilisateur spécifié
     * GET /api/dashboard/stats/{userId}
     * 
     * @param userId ID de l'utilisateur
     * @return Statistiques complètes du dashboard
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getDashboardStats(@PathVariable Long userId) {
        try {
            logger.info("Fetching dashboard stats for user {}", userId);
            DashboardStatsDto stats = dashboardService.getDashboardStats(userId);
            return ResponseEntity.ok(ApiResponse.success("Statistiques récupérées avec succès", stats));
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des statistiques: " + e.getMessage()));
        }
    }
}
