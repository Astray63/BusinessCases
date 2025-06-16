import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { BorneService } from '../../../services/borne.service';
import { ReservationService } from '../../../services/reservation.service';
import { PageTitleService } from '../../../services/page-title.service';
import { BreadcrumbComponent } from '../components/breadcrumb/breadcrumb.component';
import { forkJoin } from 'rxjs';

interface DashboardStats {
  totalUtilisateurs: number;
  totalBornes: number;
  totalReservations: number;
  utilisateursActifs: number;
  bornesActives: number;
  reservationsEnCours: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [BreadcrumbComponent, CommonModule],
  template: `
    <app-breadcrumb [items]="[{ label: 'Tableau de bord' }]"></app-breadcrumb>
    
    <div class="container">
      <h2 class="mb-4">Tableau de bord</h2>
      
      <div class="row">
        <div class="col-md-4 mb-4">
          <div class="card bg-primary text-white">
            <div class="card-body">
              <h5 class="card-title">Utilisateurs</h5>
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <h2 class="mb-0">{{stats.totalUtilisateurs}}</h2>
                  <small>Total inscrit</small>
                </div>
                <div class="text-end">
                  <h3 class="mb-0">{{stats.utilisateursActifs}}</h3>
                  <small>Actifs</small>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-md-4 mb-4">
          <div class="card bg-success text-white">
            <div class="card-body">
              <h5 class="card-title">Bornes</h5>
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <h2 class="mb-0">{{stats.totalBornes}}</h2>
                  <small>Total</small>
                </div>
                <div class="text-end">
                  <h3 class="mb-0">{{stats.bornesActives}}</h3>
                  <small>En service</small>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-md-4 mb-4">
          <div class="card bg-info text-white">
            <div class="card-body">
              <h5 class="card-title">Réservations</h5>
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <h2 class="mb-0">{{stats.totalReservations}}</h2>
                  <small>Total</small>
                </div>
                <div class="text-end">
                  <h3 class="mb-0">{{stats.reservationsEnCours}}</h3>
                  <small>En cours</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Ajoutez ici d'autres sections pour les graphiques et statistiques détaillées -->
    </div>
  `,
  styles: [`
    .card {
      border: none;
      border-radius: 10px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .card-body {
      padding: 1.5rem;
    }
    
    .card-title {
      margin-bottom: 1.5rem;
      font-size: 1.1rem;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    
    h2, h3 {
      font-weight: 600;
    }
    
    small {
      opacity: 0.8;
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats: DashboardStats = {
    totalUtilisateurs: 0,
    totalBornes: 0,
    totalReservations: 0,
    utilisateursActifs: 0,
    bornesActives: 0,
    reservationsEnCours: 0
  };

  constructor(
    private authService: AuthService,
    private borneService: BorneService,
    private reservationService: ReservationService,
    private pageTitleService: PageTitleService
  ) {}

  ngOnInit(): void {
    this.pageTitleService.setTitle('Tableau de bord');
    this.loadDashboardStats();
  }

  ngOnDestroy(): void {
    this.pageTitleService.setTitle('');
  }

  loadDashboardStats(): void {
    forkJoin({
      utilisateurs: this.authService.getAllUsers(),
      bornes: this.borneService.getAllBornes(),
      reservations: this.reservationService.getAllReservations()
    }).subscribe({
      next: (data) => {
        if (data.utilisateurs.result === 'SUCCESS' && data.utilisateurs.data) {
          this.stats.totalUtilisateurs = data.utilisateurs.data.length;
          this.stats.utilisateursActifs = data.utilisateurs.data.filter(u => u.actif).length;
        }
        
        if (data.bornes.result === 'SUCCESS' && data.bornes.data) {
          this.stats.totalBornes = data.bornes.data.length;
          this.stats.bornesActives = data.bornes.data.filter(b => b.etat === 'DISPONIBLE').length;
        }
        
        if (data.reservations.result === 'SUCCESS' && data.reservations.data) {
          this.stats.totalReservations = data.reservations.data.length;
          this.stats.reservationsEnCours = data.reservations.data.filter(
            r => r.statut === 'CONFIRMEE'
          ).length;
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    });
  }
}