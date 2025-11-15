import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserContextService } from '../../services/user-context.service';
import { BorneService } from '../../services/borne.service';
import { ReservationService } from '../../services/reservation.service';
import { Utilisateur } from '../../models/utilisateur.model';
import { Borne } from '../../models/borne.model';
import { Reservation } from '../../models/reservation.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  isProprietaire = false;
  nombreBornes = 0;
  mesBornes: Borne[] = [];
  mesReservations: Reservation[] = [];
  reservationsEnCours: Reservation[] = [];
  reservationsPassees: Reservation[] = [];
  loading = true;
  
  stats = {
    totalBornes: 0,
    bornesActives: 0,
    totalReservations: 0,
    reservationsEnCours: 0
  };

  constructor(
    private authService: AuthService,
    private userContextService: UserContextService,
    private borneService: BorneService,
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/auth/login']);
      return;
    }

    // Écouter le statut propriétaire
    this.userContextService.isProprietaire$.subscribe(isProprietaire => {
      this.isProprietaire = isProprietaire;
    });

    this.userContextService.nombreBornes$.subscribe(nombreBornes => {
      this.nombreBornes = nombreBornes;
    });

    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Charger les bornes de l'utilisateur (si propriétaire)
    if (this.isProprietaire) {
      this.loadMesBornes();
    }

    // Charger les réservations de l'utilisateur
    this.loadMesReservations();
  }

  loadMesBornes(): void {
    this.borneService.getAllBornes().subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          // Filtrer les bornes appartenant à l'utilisateur
          // Note: Vous devrez ajouter un champ ownerId dans le modèle Borne
          this.mesBornes = response.data;
          this.stats.totalBornes = this.mesBornes.length;
          this.stats.bornesActives = this.mesBornes.filter(b => b.etat === 'DISPONIBLE').length;
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement des bornes:', error);
      }
    });
  }

  loadMesReservations(): void {
    if (!this.currentUser) return;

    this.reservationService.getReservationsByCurrentUser(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.result === 'SUCCESS' && response.data) {
          this.mesReservations = response.data;
          
          // Séparer les réservations en cours et passées
          const now = new Date();
          this.reservationsEnCours = this.mesReservations.filter(r => {
            const dateFin = new Date(r.dateFin);
            return dateFin >= now && r.statut === 'CONFIRMEE';
          });
          
          this.reservationsPassees = this.mesReservations.filter(r => {
            const dateFin = new Date(r.dateFin);
            return dateFin < now || r.statut !== 'CONFIRMEE';
          });

          this.stats.totalReservations = this.mesReservations.length;
          this.stats.reservationsEnCours = this.reservationsEnCours.length;
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Erreur lors du chargement des réservations:', error);
      }
    });
  }

  ajouterBorne(): void {
    this.router.navigate(['/mes-bornes/bornes']);
  }

  modifierBorne(idBorne: number | undefined): void {
    if (!idBorne) {
      console.error('ID de borne invalide');
      return;
    }
    this.router.navigate(['/mes-bornes/bornes']);
  }

  reserverBorne(): void {
    this.router.navigate(['/client/recherche']);
  }

  annulerReservation(idReservation: number): void {
    if (!this.currentUser) {
      alert('Utilisateur non connecté');
      return;
    }
    
    if (confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) {
      this.reservationService.cancelReservation(idReservation, this.currentUser.idUtilisateur).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            alert('Réservation annulée avec succès');
            this.loadMesReservations();
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'annulation:', error);
          alert('Erreur lors de l\'annulation de la réservation');
        }
      });
    }
  }

  getStatutClass(statut: string): string {
    switch(statut) {
      case 'CONFIRMEE': return 'statut-confirmee';
      case 'ANNULEE': return 'statut-annulee';
      case 'TERMINEE': return 'statut-terminee';
      default: return '';
    }
  }

  getEtatClass(etat: string): string {
    switch(etat) {
      case 'DISPONIBLE': return 'etat-disponible';
      case 'OCCUPEE': return 'etat-occupee';
      case 'MAINTENANCE': return 'etat-maintenance';
      case 'HORS_SERVICE': return 'etat-hors-service';
      default: return '';
    }
  }
}
