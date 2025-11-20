package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.dto.DashboardStatsDto;

public interface DashboardService {
    /**
     * Récupère les statistiques du dashboard pour un utilisateur donné
     * @param userId ID de l'utilisateur
     * @return Statistiques du dashboard
     */
    DashboardStatsDto getDashboardStats(Long userId);
}
