import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ReservationService } from '../../../services/reservation.service';
import { Utilisateur } from '../../../models/utilisateur.model';
import { Reservation } from '../../../models/reservation.model';

@Component({
  selector: 'app-historique-reservations',
  templateUrl: './historique-reservations.component.html',
})
export class HistoriqueReservationsComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  reservations: Reservation[] = [];
  reservationsFiltrees: Reservation[] = [];
  isLoading = false;
  
  filtreForm: FormGroup;
  filtreActif = false;

  constructor(
    private authService: AuthService,
    private reservationService: ReservationService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.filtreForm = this.fb.group({
      statut: [''],
      borneId: [''],
      dateDebut: [''],
      dateFin: ['']
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // Plus besoin de vérifier le rôle - le ProprietaireGuard s'en occupe
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      this.chargerReservations();
    });
  }

  chargerReservations(): void {
    if (!this.currentUser) return;
    
    this.isLoading = true;
    this.reservationService.getReservationsProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.reservations = response.data
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime());
          this.reservationsFiltrees = [...this.reservations];
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement:', error);
        this.isLoading = false;
      }
    });
  }

  appliquerFiltre(): void {
    const filtres = this.filtreForm.value;
    this.reservationsFiltrees = this.reservations.filter(r => {
      if (filtres.statut && r.statut !== filtres.statut) return false;
      if (filtres.borneId && r.borne?.idBorne !== Number(filtres.borneId)) return false;
      if (filtres.dateDebut && new Date(r.dateDebut) < new Date(filtres.dateDebut)) return false;
      if (filtres.dateFin && new Date(r.dateFin) > new Date(filtres.dateFin)) return false;
      return true;
    });
    this.filtreActif = true;
  }

  reinitialiserFiltre(): void {
    this.filtreForm.reset();
    this.reservationsFiltrees = [...this.reservations];
    this.filtreActif = false;
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

  calculerDuree(dateDebut: Date, dateFin: Date): string {
    const heures = (new Date(dateFin).getTime() - new Date(dateDebut).getTime()) / (1000 * 60 * 60);
    return heures.toFixed(1) + 'h';
  }

  // Get unique bornes for filter
  getBornesUniques(): any[] {
    const bornesMap = new Map();
    this.reservations.forEach(r => {
      if (r.borne && r.borne.idBorne) {
        bornesMap.set(r.borne.idBorne, r.borne);
      }
    });
    return Array.from(bornesMap.values());
  }

  calculerRevenusFiltres(): number {
    return this.reservationsFiltrees
      .filter(r => r.statut === 'CONFIRMEE' || r.statut === 'TERMINEE')
      .reduce((sum, r) => sum + (r.montantTotal || 0), 0);
  }

  compterReservationsValidees(): number {
    return this.reservationsFiltrees.filter(r => r.statut === 'CONFIRMEE' || r.statut === 'TERMINEE').length;
  }
}
