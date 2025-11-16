package com.electriccharge.app.service;

import com.electriccharge.app.dto.DashboardStatsDto;

public interface DashboardService {
    /**
     * Récupère les statistiques du dashboard pour un utilisateur donné
     * @param userId ID de l'utilisateur
     * @return Statistiques du dashboard
     */
    DashboardStatsDto getDashboardStats(Long userId);
}
