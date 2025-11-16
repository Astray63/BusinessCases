import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { BorneService } from '../../../services/borne.service';
import { ReservationService } from '../../../services/reservation.service';
import { Utilisateur } from '../../../models/utilisateur.model';
import { Borne } from '../../../models/borne.model';
import { Reservation } from '../../../models/reservation.model';

interface StatistiquesProprietaire {
  totalBornes: number;
  bornesActives: number;
  bornesInactives: number;
  demandesEnAttente: number;
  reservationsConfirmees: number;
  revenusMoisEnCours: number;
  revenusTotaux: number;
  tauxOccupation: number;
}

@Component({
  selector: 'app-dashboard-proprietaire',
  templateUrl: './dashboard-proprietaire.component.html',
})
export class DashboardProprietaireComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  isLoading = false;
  
  stats: StatistiquesProprietaire = {
    totalBornes: 0,
    bornesActives: 0,
    bornesInactives: 0,
    demandesEnAttente: 0,
    reservationsConfirmees: 0,
    revenusMoisEnCours: 0,
    revenusTotaux: 0,
    tauxOccupation: 0
  };

  mesBornes: Borne[] = [];
  dernieresReservations: Reservation[] = [];
  demandesRecentes: Reservation[] = [];

  constructor(
    private authService: AuthService,
    private borneService: BorneService,
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // Plus besoin de vérifier le rôle - le ProprietaireGuard s'en occupe
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      this.chargerDonnees();
    });
  }

  chargerDonnees(): void {
    if (!this.currentUser) return;
    
    this.isLoading = true;
    
    // Charger les bornes
    this.borneService.getBornesByProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.mesBornes = response.data;
          this.calculerStatistiquesBornes();
        }
      },
      error: (error: any) => console.error('Erreur lors du chargement des bornes:', error)
    });

    // Charger les réservations
    this.reservationService.getReservationsProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          const reservations = response.data;
          
          // Demandes en attente
          this.demandesRecentes = reservations
            .filter(r => r.statut === 'EN_ATTENTE')
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime())
            .slice(0, 5);
          
          // Dernières réservations confirmées
          this.dernieresReservations = reservations
            .filter(r => r.statut === 'CONFIRMEE' || r.statut === 'TERMINEE')
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime())
            .slice(0, 5);
          
          this.calculerStatistiquesReservations(reservations);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des réservations:', error);
        this.isLoading = false;
      }
    });
  }

  calculerStatistiquesBornes(): void {
    this.stats.totalBornes = this.mesBornes.length;
    this.stats.bornesActives = this.mesBornes.filter(b => b.etat === 'DISPONIBLE').length;
    this.stats.bornesInactives = this.mesBornes.filter(b => b.etat !== 'DISPONIBLE').length;
  }

  calculerStatistiquesReservations(reservations: Reservation[]): void {
    this.stats.demandesEnAttente = reservations.filter(r => r.statut === 'EN_ATTENTE').length;
    this.stats.reservationsConfirmees = reservations.filter(r => r.statut === 'CONFIRMEE').length;
    
    // Revenus du mois en cours
    const debutMois = new Date();
    debutMois.setDate(1);
    debutMois.setHours(0, 0, 0, 0);
    
    this.stats.revenusMoisEnCours = reservations
      .filter(r => 
        (r.statut === 'CONFIRMEE' || r.statut === 'TERMINEE') && 
        new Date(r.dateDebut) >= debutMois
      )
      .reduce((sum, r) => sum + (r.montantTotal || 0), 0);
    
    // Revenus totaux
    this.stats.revenusTotaux = reservations
      .filter(r => r.statut === 'CONFIRMEE' || r.statut === 'TERMINEE')
      .reduce((sum, r) => sum + (r.montantTotal || 0), 0);
    
    // Taux d'occupation (simplifié)
    const reservationsActives = reservations.filter(r => 
      r.statut === 'CONFIRMEE' && 
      new Date(r.dateFin) > new Date()
    ).length;
    
    this.stats.tauxOccupation = this.stats.totalBornes > 0 
      ? Math.round((reservationsActives / this.stats.totalBornes) * 100) 
      : 0;
  }

  naviguerVers(route: string): void {
    this.router.navigate([`/proprietaire/${route}`]);
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'EN_ATTENTE': 'badge-warning',
      'CONFIRMEE': 'badge-success',
      'TERMINEE': 'badge-secondary',
      'ANNULEE': 'badge-danger',
      'REFUSEE': 'badge-danger'
    };
    return classes[statut] || 'badge-secondary';
  }

  getStatutLabel(statut: string): string {
    const labels: { [key: string]: string } = {
      'EN_ATTENTE': 'En attente',
      'CONFIRMEE': 'Confirmée',
      'TERMINEE': 'Terminée',
      'ANNULEE': 'Annulée',
      'REFUSEE': 'Refusée'
    };
    return labels[statut] || statut;
  }

  getEtatClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'DISPONIBLE': 'badge-success',
      'OCCUPEE': 'badge-warning',
      'HORS_SERVICE': 'badge-danger',
      'EN_MAINTENANCE': 'badge-secondary'
    };
    return classes[etat] || 'badge-secondary';
  }

  accepterDemande(idReservation: number): void {
    if (!this.currentUser) return;
    
    this.isLoading = true;
    this.reservationService.accepterReservation(idReservation, this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          alert('Réservation acceptée avec succès !');
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors de l\'acceptation:', error);
        alert('Erreur lors de l\'acceptation de la réservation');
        this.isLoading = false;
      }
    });
  }

  refuserDemande(idReservation: number): void {
    const motif = prompt('Motif du refus (optionnel):');
    if (motif === null) return; // Annulation
    
    if (!this.currentUser) return;
    
    this.isLoading = true;
    this.reservationService.refuserReservation(idReservation, this.currentUser.idUtilisateur, motif).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          alert('Réservation refusée');
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors du refus:', error);
        alert('Erreur lors du refus de la réservation');
        this.isLoading = false;
      }
    });
  }

  calculerDuree(dateDebut: Date | string, dateFin: Date | string): number {
    const debut = new Date(dateDebut);
    const fin = new Date(dateFin);
    const diffMs = fin.getTime() - debut.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    return Math.round(diffHours * 10) / 10; // Arrondi à 1 décimale
  }
}
