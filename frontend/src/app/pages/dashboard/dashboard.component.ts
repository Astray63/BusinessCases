import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserContextService } from '../../services/user-context.service';
import { DashboardService } from '../../services/dashboard.service';
import { Utilisateur } from '../../models/utilisateur.model';
import { DashboardStats } from '../../models/dashboard-stats.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  isProprietaire = false;
  nombreBornes = 0;
  loading = true;
  dashboardStats: DashboardStats | null = null;

  constructor(
    private authService: AuthService,
    private userContextService: UserContextService,
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Récupérer l'utilisateur courant
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      // Charger les statistiques du dashboard
      this.loadDashboardData();
    });

    // Écouter le statut propriétaire
    this.userContextService.isProprietaire$.subscribe(isProprietaire => {
      this.isProprietaire = isProprietaire;
    });

    this.userContextService.nombreBornes$.subscribe(nombreBornes => {
      this.nombreBornes = nombreBornes;
    });
  }

  loadDashboardData(): void {
    if (!this.currentUser) return;
    
    this.loading = true;
    
    this.dashboardService.getDashboardStats(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.result === 'SUCCESS' && response.data) {
          this.dashboardStats = response.data;
          
          // Mettre à jour le contexte si l'utilisateur est propriétaire
          if (this.dashboardStats.ownerStats) {
            this.isProprietaire = true;
            this.nombreBornes = this.dashboardStats.ownerStats.totalBornes;
          }
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    });
  }

  // Navigation methods
  ajouterBorne(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }

  reserverBorne(): void {
    this.router.navigate(['/client/recherche']);
  }

  voirMesBornes(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }

  voirMesReservations(): void {
    this.router.navigate(['/client/mes-reservations']);
  }

  voirDemandesReservation(): void {
    this.router.navigate(['/proprietaire/demandes']);
  }

  devenirProprietaire(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }
}
