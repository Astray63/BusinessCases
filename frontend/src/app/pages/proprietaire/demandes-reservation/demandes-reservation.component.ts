import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ReservationService } from '../../../services/reservation.service';
import { ToastService } from '../../../services/toast.service';
import { Utilisateur } from '../../../models/utilisateur.model';
import { Reservation } from '../../../models/reservation.model';

@Component({
  selector: 'app-demandes-reservation',
  templateUrl: './demandes-reservation.component.html',
})
export class DemandesReservationComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  demandes: Reservation[] = [];
  isLoading = false;

  constructor(
    private authService: AuthService,
    private reservationService: ReservationService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // Plus besoin de vérifier le rôle - le ProprietaireGuard s'en occupe
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      this.chargerDemandes();
    });
  }

  chargerDemandes(): void {
    if (!this.currentUser) return;
    
    this.isLoading = true;
    
    this.reservationService.getReservationsProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.demandes = response.data
            .filter(r => r.statut === 'EN_ATTENTE')
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime());
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
      }
    });
  }

  accepterDemande(idReservation: number): void {
    if (!this.currentUser || !confirm('Accepter cette réservation ?')) return;
    
    this.isLoading = true;
    this.reservationService.accepterReservation(idReservation, this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Réservation acceptée avec succès !');
          this.chargerDemandes();
        }
      },
      error: (error) => {
        this.toastService.showError('Erreur lors de l\'acceptation de la réservation');
        this.isLoading = false;
      }
    });
  }

  refuserDemande(idReservation: number): void {
    const motif = prompt('Motif du refus (optionnel) :');
    if (motif === null) return; // Annulation
    
    if (!this.currentUser) return;
    
    this.isLoading = true;
    this.reservationService.refuserReservation(idReservation, this.currentUser.idUtilisateur, motif).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Réservation refusée');
          this.chargerDemandes();
        }
      },
      error: (error) => {
        this.toastService.showError('Erreur lors du refus de la réservation');
        this.isLoading = false;
      }
    });
  }

  calculerDuree(dateDebut: Date, dateFin: Date): string {
    const heures = (new Date(dateFin).getTime() - new Date(dateDebut).getTime()) / (1000 * 60 * 60);
    return heures.toFixed(1) + 'h';
  }
}
