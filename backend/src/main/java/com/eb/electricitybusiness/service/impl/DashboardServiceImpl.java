package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.dto.DashboardStatsDto;
import com.eb.electricitybusiness.dto.DashboardStatsDto.ClientStats;
import com.eb.electricitybusiness.dto.DashboardStatsDto.OwnerStats;
import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.service.BorneService;
import com.eb.electricitybusiness.service.DashboardService;
import com.eb.electricitybusiness.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

        private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

        private final ReservationRepository reservationRepository;
        private final BorneRepository borneRepository;
        // private final UtilisateurRepository utilisateurRepository;
        private final ReservationService reservationService;
        private final BorneService borneService;

        public DashboardServiceImpl(
                        ReservationRepository reservationRepository,
                        BorneRepository borneRepository,
                        // UtilisateurRepository utilisateurRepository,
                        ReservationService reservationService,
                        BorneService borneService) {
                this.reservationRepository = reservationRepository;
                this.borneRepository = borneRepository;
                // this.utilisateurRepository = utilisateurRepository;
                this.reservationService = reservationService;
                this.borneService = borneService;
        }

        @Override
        public DashboardStatsDto getDashboardStats(Long userId) {
                logger.debug("Fetching dashboard stats for user {}", userId);

                DashboardStatsDto stats = new DashboardStatsDto();

                // Charger les statistiques client
                stats.setClientStats(calculateClientStats(userId));

                // Charger les réservations récentes (les 5 dernières)
                List<ReservationDto> allReservations = reservationService.getByUser(userId);
                stats.setRecentReservations(allReservations.stream()
                                .sorted((r1, r2) -> r2.getDateDebut().compareTo(r1.getDateDebut()))
                                .limit(5)
                                .collect(Collectors.toList()));

                // Vérifier si l'utilisateur est propriétaire
                List<Borne> userBornes = borneRepository.findByOwnerIdUtilisateur(userId);
                if (!userBornes.isEmpty()) {
                        stats.setOwnerStats(calculateOwnerStats(userId, userBornes));

                        // Charger les bornes récentes (les 5 dernières)
                        List<Borne> allBornes = borneService.getBornesByOwner(userId);
                        stats.setRecentBornes(allBornes.stream()
                                        .map(this::convertBorneToBorneDto)
                                        .limit(5)
                                        .collect(Collectors.toList()));
                }

                return stats;
        }

        private ClientStats calculateClientStats(Long userId) {
                ClientStats stats = new ClientStats();

                List<Reservation> allReservations = reservationRepository.findByUtilisateur_IdUtilisateur(userId);

                // Statistiques générales
                stats.setTotalReservations(allReservations.size());

                // Compter par état
                Map<Reservation.EtatReservation, Long> reservationsByStatus = allReservations.stream()
                                .collect(Collectors.groupingBy(Reservation::getEtat, Collectors.counting()));

                stats.setReservationsEnCours(
                                reservationsByStatus.getOrDefault(Reservation.EtatReservation.ACTIVE, 0L).intValue());
                stats.setReservationsTerminees(
                                reservationsByStatus.getOrDefault(Reservation.EtatReservation.TERMINEE, 0L).intValue());
                stats.setReservationsAnnulees(
                                reservationsByStatus.getOrDefault(Reservation.EtatReservation.ANNULEE, 0L).intValue());
                stats.setReservationsConfirmees(stats.getReservationsEnCours());

                // Montants
                BigDecimal montantTotal = allReservations.stream()
                                .filter(r -> r.getTotalPrice() != null)
                                .map(Reservation::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setMontantTotalDepense(montantTotal);

                // Montant du mois en cours
                LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

                BigDecimal montantMois = allReservations.stream()
                                .filter(r -> r.getCreatedAt() != null &&
                                                r.getCreatedAt().isAfter(startOfMonth) &&
                                                r.getCreatedAt().isBefore(endOfMonth))
                                .filter(r -> r.getTotalPrice() != null)
                                .map(Reservation::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setMontantMoisEnCours(montantMois);

                // Réservations ce mois
                long reservationsMois = allReservations.stream()
                                .filter(r -> r.getCreatedAt() != null &&
                                                r.getCreatedAt().isAfter(startOfMonth) &&
                                                r.getCreatedAt().isBefore(endOfMonth))
                                .count();
                stats.setReservationsCeMois((int) reservationsMois);

                // Prochaine réservation
                Optional<Reservation> nextReservation = allReservations.stream()
                                .filter(r -> r.getEtat() == Reservation.EtatReservation.ACTIVE)
                                .filter(r -> r.getDateDebut().isAfter(LocalDateTime.now()))
                                .min(Comparator.comparing(Reservation::getDateDebut));

                nextReservation.ifPresent(reservation -> {
                        try {
                                ReservationDto nextResDto = reservationService
                                                .getById(reservation.getNumeroReservation());
                                stats.setProchaineReservation(nextResDto);
                        } catch (Exception e) {
                                logger.warn("Could not load next reservation details", e);
                        }
                });

                return stats;
        }

        private OwnerStats calculateOwnerStats(Long userId, List<Borne> bornes) {
                OwnerStats stats = new OwnerStats();

                stats.setTotalBornes(bornes.size());

                // Compter par état
                Map<Borne.Etat, Long> bornesByStatus = bornes.stream()
                                .collect(Collectors.groupingBy(Borne::getEtat, Collectors.counting()));

                stats.setBornesDisponibles(
                                bornesByStatus.getOrDefault(Borne.Etat.DISPONIBLE, 0L).intValue());
                stats.setBornesOccupees(bornesByStatus.getOrDefault(Borne.Etat.OCCUPEE, 0L).intValue());
                stats.setBornesMaintenance(
                                bornesByStatus.getOrDefault(Borne.Etat.EN_MAINTENANCE, 0L).intValue());
                stats.setBornesHorsService(bornesByStatus.getOrDefault(Borne.Etat.EN_PANNE, 0L).intValue());

                // Récupérer toutes les réservations pour les bornes du propriétaire
                List<Long> borneIds = bornes.stream()
                                .map(Borne::getIdBorne)
                                .collect(Collectors.toList());

                List<Reservation> allReservations = borneIds.stream()
                                .flatMap(id -> reservationRepository.findByBorneIdBorne(id).stream())
                                .collect(Collectors.toList());

                stats.setTotalReservations(allReservations.size());

                // Demandes en attente (réservations actives à venir)
                long demandesEnAttente = allReservations.stream()
                                .filter(r -> r.getEtat() == Reservation.EtatReservation.ACTIVE)
                                .filter(r -> r.getDateDebut().isAfter(LocalDateTime.now()))
                                .count();
                stats.setDemandesEnAttente((int) demandesEnAttente);

                // Réservations confirmées en cours
                long reservationsConfirmees = allReservations.stream()
                                .filter(r -> r.getEtat() == Reservation.EtatReservation.ACTIVE)
                                .count();
                stats.setReservationsConfirmees((int) reservationsConfirmees);

                // Calcul des revenus
                LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

                BigDecimal revenusEstimesMois = allReservations.stream()
                                .filter(r -> r.getCreatedAt() != null &&
                                                r.getCreatedAt().isAfter(startOfMonth) &&
                                                r.getCreatedAt().isBefore(endOfMonth))
                                .filter(r -> r.getTotalPrice() != null)
                                .map(Reservation::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setRevenusEstimesMois(revenusEstimesMois);

                BigDecimal revenusTotaux = allReservations.stream()
                                .filter(r -> r.getTotalPrice() != null)
                                .map(Reservation::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setRevenusTotaux(revenusTotaux);

                // Borne la plus réservée
                Map<Long, Long> reservationsByBorne = allReservations.stream()
                                .collect(Collectors.groupingBy(r -> r.getBorne().getIdBorne(),
                                                Collectors.counting()));

                if (!reservationsByBorne.isEmpty()) {
                        Optional<Map.Entry<Long, Long>> mostReserved = reservationsByBorne.entrySet().stream()
                                        .max(Map.Entry.comparingByValue());

                        mostReserved.ifPresent(entry -> {
                                try {
                                        Borne borne = borneService.getBorneById(entry.getKey());
                                        BorneDto borneDto = convertBorneToBorneDto(borne);
                                        stats.setBorneLaPlusReservee(borneDto);
                                } catch (Exception e) {
                                        logger.warn("Could not load most reserved station details", e);
                                }
                        });
                }

                // Taux d'occupation moyen (simplifié)
                if (!bornes.isEmpty()) {
                        double tauxOccupation = (double) stats.getBornesOccupees() / bornes.size() * 100;
                        stats.setTauxOccupationMoyen(Math.round(tauxOccupation * 100.0) / 100.0);
                }

                return stats;
        }

        private BorneDto convertBorneToBorneDto(Borne borne) {
                BorneDto borneDto = new BorneDto();
                borneDto.setId(borne.getIdBorne());
                borneDto.setNumero(borne.getNumero());
                borneDto.setNom(borne.getNom());
                borneDto.setLocalisation(borne.getLocalisation());
                borneDto.setLatitude(borne.getLatitude());
                borneDto.setLongitude(borne.getLongitude());
                borneDto.setPrixALaMinute(borne.getPrixALaMinute());
                borneDto.setPuissance(borne.getPuissance());
                borneDto.setInstructionSurPied(borne.getInstructionSurPied());
                borneDto.setDescription(borne.getDescription());
                borneDto.setEtat(borne.getEtat().name());
                borneDto.setOccupee(borne.getOccupee());
                borneDto.setOwnerId(borne.getOwner() != null ? borne.getOwner().getIdUtilisateur() : null);
                return borneDto;
        }
}
