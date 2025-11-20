package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour nettoyer automatiquement les réservations en attente expirées.
 * Les réservations en attente depuis plus de 24 heures sont automatiquement annulées.
 */
@Service
public class ReservationCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationCleanupService.class);
    private static final int EXPIRATION_HOURS = 24;
    
    private final ReservationRepository reservationRepository;
    
    public ReservationCleanupService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
    
    /**
     * Nettoie les réservations en attente expirées.
     * Exécuté toutes les heures.
     */
    @Scheduled(cron = "0 0 * * * ?") // Toutes les heures
    @Transactional
    public void cleanupExpiredPendingReservations() {
        try {
            logger.info("Démarrage du nettoyage des réservations en attente expirées");
            
            LocalDateTime expirationTime = LocalDateTime.now().minusHours(EXPIRATION_HOURS);
            List<Reservation> expiredReservations = reservationRepository.findExpiredPendingReservations(expirationTime);
            
            if (expiredReservations.isEmpty()) {
                logger.info("Aucune réservation en attente expirée trouvée");
                return;
            }
            
            for (Reservation reservation : expiredReservations) {
                reservation.setEtat(Reservation.EtatReservation.ANNULEE);
                logger.info("Réservation #{} annulée automatiquement (en attente depuis {})", 
                    reservation.getNumeroReservation(), 
                    reservation.getCreatedAt());
            }
            
            reservationRepository.saveAll(expiredReservations);
            logger.info("{} réservation(s) en attente expirée(s) annulée(s)", expiredReservations.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des réservations en attente expirées", e);
        }
    }
    
    /**
     * Nettoie les réservations en attente dont la date de début est déjà passée.
     * Exécuté toutes les 30 minutes.
     */
    @Scheduled(cron = "0 */30 * * * ?") // Toutes les 30 minutes
    @Transactional
    public void cleanupPastPendingReservations() {
        try {
            logger.info("Démarrage du nettoyage des réservations en attente dont la date est passée");
            
            LocalDateTime now = LocalDateTime.now();
            List<Reservation> pastReservations = reservationRepository.findPastPendingReservations(now);
            
            if (pastReservations.isEmpty()) {
                logger.info("Aucune réservation en attente avec date passée trouvée");
                return;
            }
            
            for (Reservation reservation : pastReservations) {
                reservation.setEtat(Reservation.EtatReservation.ANNULEE);
                logger.info("Réservation #{} annulée automatiquement (date de début passée: {})", 
                    reservation.getNumeroReservation(), 
                    reservation.getDateDebut());
            }
            
            reservationRepository.saveAll(pastReservations);
            logger.info("{} réservation(s) en attente avec date passée annulée(s)", pastReservations.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des réservations en attente avec date passée", e);
        }
    }
}
