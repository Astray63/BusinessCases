import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../services/reservation.service';
import { ToastService } from '../../../services/toast.service';
import { PageTitleService } from '../../../services/page-title.service';
import { BreadcrumbComponent } from '../components/breadcrumb/breadcrumb.component';
import { Reservation, ReservationStatus } from '../../../models/reservation.model';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [CommonModule, BreadcrumbComponent],
  template: `
    <app-breadcrumb [items]="[{ label: 'Gestion des réservations' }]"></app-breadcrumb>

    <div class="container mt-4">
      <h2>Gestion des réservations</h2>

      <div *ngIf="loading" class="text-center my-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
        <p class="mt-2">Chargement des réservations...</p>
      </div>

      <div *ngIf="!loading" class="table-responsive">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>Utilisateur</th>
              <th>Borne</th>
              <th>Date début</th>
              <th>Date fin</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let reservation of reservations">
              <td>{{reservation.idReservation}}</td>
              <td>{{reservation.utilisateur.pseudo}}</td>
              <td>{{reservation.borne.localisation}}</td>
              <td>{{reservation.dateDebut | date:'dd/MM/yyyy HH:mm'}}</td>
              <td>{{reservation.dateFin | date:'dd/MM/yyyy HH:mm'}}</td>
              <td>
                <span [class]="getStatusBadgeClass(reservation.statut)">
                  {{reservation.statut}}
                </span>
              </td>
              <td>
                <div class="btn-group" [class.disabled]="actionInProgress">
                  <button 
                    class="btn btn-sm btn-primary me-2" 
                    (click)="editReservation(reservation)"
                    [disabled]="actionInProgress || reservation.statut === 'TERMINEE' || reservation.statut === 'ANNULEE'">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button 
                    class="btn btn-sm btn-danger" 
                    (click)="cancelReservation(reservation)"
                    [disabled]="actionInProgress || reservation.statut === 'TERMINEE' || reservation.statut === 'ANNULEE'">
                    <i class="bi bi-x-circle"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div *ngIf="reservations.length === 0" class="text-center my-5">
          <p>Aucune réservation trouvée</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .table {
      margin-top: 1rem;
    }
    .btn-sm {
      padding: 0.25rem 0.5rem;
    }
    .badge {
      padding: 0.5em 0.75em;
    }
    .btn-group.disabled {
      pointer-events: none;
      opacity: 0.65;
    }
    .spinner-border {
      width: 3rem;
      height: 3rem;
    }
  `]
})
export class ReservationsAdminComponent implements OnInit, OnDestroy {
  reservations: Reservation[] = [];
  loading = false;
  actionInProgress = false;

  constructor(
    private reservationService: ReservationService,
    private toastService: ToastService,
    private pageTitleService: PageTitleService
  ) {}

  ngOnInit(): void {
    this.pageTitleService.setTitle('Gestion des réservations');
    this.loadReservations();
  }

  ngOnDestroy(): void {
    this.pageTitleService.setTitle('');
  }

  loadReservations(): void {
    this.loading = true;
    this.reservationService.getAllReservations().subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.reservations = response.data ?? [];
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement des réservations:', error);
        this.toastService.showError('Erreur lors du chargement des réservations');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  editReservation(reservation: Reservation): void {
    if (reservation.statut === 'TERMINEE' || reservation.statut === 'ANNULEE') {
      this.toastService.showWarning('Cette réservation ne peut plus être modifiée');
      return;
    }
    // TODO: Implémenter la logique de modification
    console.log('Modification de la réservation:', reservation);
    this.toastService.showWarning('La modification des réservations n\'est pas encore implémentée');
  }

  cancelReservation(reservation: Reservation): void {
    if (reservation.statut === 'TERMINEE' || reservation.statut === 'ANNULEE') {
      this.toastService.showWarning('Cette réservation ne peut plus être annulée');
      return;
    }

    if (confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) {
      this.actionInProgress = true;
      // L'admin peut annuler sans passer de requesterId, le backend utilisera le contexte d'auth
      this.reservationService.cancelReservation(reservation.idReservation, reservation.utilisateur?.idUtilisateur).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            this.loadReservations();
            this.toastService.showSuccess('La réservation a été annulée avec succès');
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'annulation de la réservation:', error);
          this.toastService.showError('Erreur lors de l\'annulation de la réservation');
        },
        complete: () => {
          this.actionInProgress = false;
        }
      });
    }
  }

  getStatusBadgeClass(status: ReservationStatus): string {
    const classes: Record<ReservationStatus, string> = {
      'EN_ATTENTE': 'badge bg-warning',
      'CONFIRMEE': 'badge bg-success',
      'ANNULEE': 'badge bg-danger',
      'TERMINEE': 'badge bg-secondary',
      'REFUSEE': 'badge bg-danger',
      'ACTIVE': 'badge bg-primary'
    };
    return classes[status] || 'badge bg-secondary';
  }
}