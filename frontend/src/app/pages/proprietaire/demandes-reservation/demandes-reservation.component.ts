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
    console.log('🔍 Chargement des demandes pour le propriétaire:', this.currentUser.idUtilisateur);
    
    this.reservationService.getReservationsProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        console.log('📦 Réponse brute du serveur:', response);
        
        if (response.result === 'SUCCESS' && response.data) {
          console.log('✅ Données reçues:', response.data);
          console.log('📊 Total de réservations:', response.data.length);
          
          // Afficher les statuts de toutes les réservations
          response.data.forEach((r, index) => {
            console.log(`   [${index}] Réservation #${r.idReservation} - Statut: "${r.statut}" - Borne: ${r.borne?.localisation}`);
          });
          
          this.demandes = response.data
            .filter(r => {
              const isEnAttente = r.statut === 'EN_ATTENTE';
              console.log(`   Réservation #${r.idReservation}: statut="${r.statut}", EN_ATTENTE=${isEnAttente}`);
              return isEnAttente;
            })
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime());
          
          console.log('✨ Demandes en attente filtrées:', this.demandes.length);
        } else {
          console.warn('⚠️ Aucune donnée ou réponse non SUCCESS:', response);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des demandes:', error);
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
        console.error('Erreur lors de l\'acceptation:', error);
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
        console.error('Erreur lors du refus:', error);
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
