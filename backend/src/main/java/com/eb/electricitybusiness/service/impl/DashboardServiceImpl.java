package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.dto.DashboardStatsDto;
import com.eb.electricitybusiness.dto.DashboardStatsDto.ClientStats;
import com.eb.electricitybusiness.dto.DashboardStatsDto.OwnerStats;
import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.model.ChargingStation;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.repository.ChargingStationRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
import com.eb.electricitybusiness.service.ChargingStationService;
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
        private final ChargingStationRepository chargingStationRepository;
        // private final UtilisateurRepository utilisateurRepository;
        private final ReservationService reservationService;
        private final ChargingStationService chargingStationService;

        public DashboardServiceImpl(
                        ReservationRepository reservationRepository,
                        ChargingStationRepository chargingStationRepository,
                        // UtilisateurRepository utilisateurRepository,
                        ReservationService reservationService,
                        ChargingStationService chargingStationService) {
                this.reservationRepository = reservationRepository;
                this.chargingStationRepository = chargingStationRepository;
                // this.utilisateurRepository = utilisateurRepository;
                this.reservationService = reservationService;
                this.chargingStationService = chargingStationService;
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
                List<ChargingStation> userStations = chargingStationRepository.findByOwner_IdUtilisateur(userId);
                if (!userStations.isEmpty()) {
                        stats.setOwnerStats(calculateOwnerStats(userId, userStations));

                        // Charger les bornes récentes (les 5 dernières)
                        List<ChargingStationDto> allBornes = chargingStationService.getByOwner(userId);
                        stats.setRecentBornes(allBornes.stream()
                                        .map(this::convertChargingStationDtoToBorneDto)
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

        private OwnerStats calculateOwnerStats(Long userId, List<ChargingStation> stations) {
                OwnerStats stats = new OwnerStats();

                stats.setTotalBornes(stations.size());

                // Compter par état
                Map<ChargingStation.Etat, Long> stationsByStatus = stations.stream()
                                .collect(Collectors.groupingBy(ChargingStation::getEtat, Collectors.counting()));

                stats.setBornesDisponibles(
                                stationsByStatus.getOrDefault(ChargingStation.Etat.DISPONIBLE, 0L).intValue());
                stats.setBornesOccupees(stationsByStatus.getOrDefault(ChargingStation.Etat.OCCUPEE, 0L).intValue());
                stats.setBornesMaintenance(
                                stationsByStatus.getOrDefault(ChargingStation.Etat.EN_MAINTENANCE, 0L).intValue());
                stats.setBornesHorsService(stationsByStatus.getOrDefault(ChargingStation.Etat.EN_PANNE, 0L).intValue());

                // Récupérer toutes les réservations pour les bornes du propriétaire
                List<Long> stationIds = stations.stream()
                                .map(ChargingStation::getIdBorne)
                                .collect(Collectors.toList());

                List<Reservation> allReservations = stationIds.stream()
                                .flatMap(id -> reservationRepository.findByChargingStation_IdBorne(id).stream())
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
                Map<Long, Long> reservationsByStation = allReservations.stream()
                                .collect(Collectors.groupingBy(r -> r.getChargingStation().getIdBorne(),
                                                Collectors.counting()));

                if (!reservationsByStation.isEmpty()) {
                        Optional<Map.Entry<Long, Long>> mostReserved = reservationsByStation.entrySet().stream()
                                        .max(Map.Entry.comparingByValue());

                        mostReserved.ifPresent(entry -> {
                                try {
                                        ChargingStationDto stationDto = chargingStationService.getById(entry.getKey());
                                        BorneDto borneDto = convertChargingStationDtoToBorneDto(stationDto);
                                        stats.setBorneLaPlusReservee(borneDto);
                                } catch (Exception e) {
                                        logger.warn("Could not load most reserved station details", e);
                                }
                        });
                }

                // Taux d'occupation moyen (simplifié)
                if (!stations.isEmpty()) {
                        double tauxOccupation = (double) stats.getBornesOccupees() / stations.size() * 100;
                        stats.setTauxOccupationMoyen(Math.round(tauxOccupation * 100.0) / 100.0);
                }

                return stats;
        }

        private BorneDto convertChargingStationDtoToBorneDto(ChargingStationDto csDto) {
                BorneDto borneDto = new BorneDto();
                borneDto.setId(csDto.getId());
                borneDto.setNumero(csDto.getNumero());
                borneDto.setNom(csDto.getNom());
                borneDto.setLocalisation(csDto.getLocalisation());
                borneDto.setLatitude(csDto.getLatitude());
                borneDto.setLongitude(csDto.getLongitude());
                borneDto.setPrixALaMinute(csDto.getPrixALaMinute());
                borneDto.setPuissance(csDto.getPuissance());
                borneDto.setInstructionSurPied(csDto.getInstructionSurPied());
                borneDto.setDescription(csDto.getDescription());
                borneDto.setEtat(csDto.getEtat());
                borneDto.setOccupee(csDto.getOccupee());
                borneDto.setOwnerId(csDto.getOwnerId());
                return borneDto;
        }
}
