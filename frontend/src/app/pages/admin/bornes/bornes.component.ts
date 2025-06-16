import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BorneService } from '../../../services/borne.service';
import { ToastService } from '../../../services/toast.service';
import { PageTitleService } from '../../../services/page-title.service';
import { Borne } from '../../../models/borne.model';
import { BorneFormModalComponent } from './borne-form-modal/borne-form-modal.component';
import { BreadcrumbComponent } from '../components/breadcrumb/breadcrumb.component';
import { CommonModule } from '@angular/common';

type BorneStatus = 'DISPONIBLE' | 'OCCUPE' | 'HORS_SERVICE';

@Component({
  selector: 'app-admin-bornes',
  standalone: true,
  imports: [BreadcrumbComponent, CommonModule],
  template: `
    <app-breadcrumb [items]="[{ label: 'Gestion des bornes' }]"></app-breadcrumb>
    
    <div class="container mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Gestion des bornes</h2>
        <button class="btn btn-success" (click)="openBorneModal()" [disabled]="loading">
          <i class="bi bi-plus-circle me-2"></i>Ajouter une borne
        </button>
      </div>

      <div *ngIf="loading" class="text-center my-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
        <p class="mt-2">Chargement des bornes...</p>
      </div>

      <div *ngIf="!loading" class="table-responsive">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>Localisation</th>
              <th>Type</th>
              <th>Puissance</th>
              <th>Prix</th>
              <th>État</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let borne of bornes">
              <td>{{borne.idBorne}}</td>
              <td>{{borne.localisation}}</td>
              <td>{{borne.type}}</td>
              <td>{{borne.puissance}} kW</td>
              <td>{{borne.prix}} €/kWh</td>
              <td>
                <span [class]="getStatusBadgeClass(borne.etat)">
                  {{borne.etat}}
                </span>
              </td>
              <td>
                <div class="btn-group" [class.disabled]="actionInProgress">
                  <button class="btn btn-sm btn-primary me-2" 
                          (click)="openBorneModal(borne)"
                          [disabled]="actionInProgress">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm" 
                          [class.btn-warning]="borne.etat === 'DISPONIBLE'"
                          [class.btn-success]="borne.etat === 'HORS_SERVICE'"
                          (click)="toggleBorneStatus(borne)"
                          [disabled]="actionInProgress">
                    <i class="bi" 
                       [class.bi-toggle-on]="borne.etat === 'DISPONIBLE'" 
                       [class.bi-toggle-off]="borne.etat === 'HORS_SERVICE'">
                    </i>
                  </button>
                  <button class="btn btn-sm btn-danger ms-2" 
                          (click)="deleteBorne(borne)"
                          [disabled]="actionInProgress">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div *ngIf="bornes.length === 0" class="text-center my-5">
          <p>Aucune borne trouvée</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .badge {
      padding: 0.5em 0.75em;
    }
    .btn-group .btn-sm {
      padding: 0.25rem 0.5rem;
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
export class BornesAdminComponent implements OnInit, OnDestroy {
  bornes: Borne[] = [];
  loading = false;
  actionInProgress = false;

  constructor(
    private borneService: BorneService,
    private modalService: NgbModal,
    private toastService: ToastService,
    private pageTitleService: PageTitleService
  ) {}

  ngOnInit(): void {
    this.pageTitleService.setTitle('Gestion des bornes');
    this.loadBornes();
  }

  ngOnDestroy(): void {
    this.pageTitleService.setTitle('');
  }

  loadBornes(): void {
    this.loading = true;
    this.borneService.getAllBornes().subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.bornes = response.data;
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement des bornes:', error);
        this.toastService.showError('Erreur lors du chargement des bornes');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  openBorneModal(borne?: Borne): void {
    const modalRef = this.modalService.open(BorneFormModalComponent);
    modalRef.componentInstance.borne = borne;

    modalRef.result.then((result) => {
      if (result) {
        if (result.idBorne) {
          this.updateBorne(result);
        } else {
          this.createBorne(result);
        }
      }
    }).catch(() => {});
  }

  createBorne(borne: Partial<Borne>): void {
    this.actionInProgress = true;
    this.borneService.createBorne(borne).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.loadBornes();
          this.toastService.showSuccess('La borne a été créée avec succès');
        }
      },
      error: (error) => {
        console.error('Erreur lors de la création de la borne:', error);
        this.toastService.showError('Erreur lors de la création de la borne');
      },
      complete: () => {
        this.actionInProgress = false;
      }
    });
  }

  updateBorne(borne: Borne): void {
    this.actionInProgress = true;
    this.borneService.updateBorne(borne.idBorne, borne).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.loadBornes();
          this.toastService.showSuccess('La borne a été mise à jour avec succès');
        }
      },
      error: (error) => {
        console.error('Erreur lors de la mise à jour de la borne:', error);
        this.toastService.showError('Erreur lors de la mise à jour de la borne');
      },
      complete: () => {
        this.actionInProgress = false;
      }
    });
  }

  deleteBorne(borne: Borne): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette borne ?')) {
      this.actionInProgress = true;
      this.borneService.deleteBorne(borne.idBorne).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            this.loadBornes();
            this.toastService.showSuccess('La borne a été supprimée avec succès');
          }
        },
        error: (error) => {
          console.error('Erreur lors de la suppression de la borne:', error);
          this.toastService.showError('Erreur lors de la suppression de la borne');
        },
        complete: () => {
          this.actionInProgress = false;
        }
      });
    }
  }

  toggleBorneStatus(borne: Borne): void {
    const action = borne.etat === 'DISPONIBLE' ? 'mettre hors service' : 'remettre en service';
    if (confirm(`Êtes-vous sûr de vouloir ${action} cette borne ?`)) {
      this.actionInProgress = true;
      const method = borne.etat === 'DISPONIBLE' ? 'marquerHorsService' : 'marquerEnService';
      this.borneService[method](borne.idBorne).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            this.loadBornes();
            this.toastService.showSuccess(`La borne a été ${borne.etat === 'DISPONIBLE' ? 'mise hors service' : 'remise en service'} avec succès`);
          }
        },
        error: (error) => {
          console.error('Erreur lors du changement de statut de la borne:', error);
          this.toastService.showError(`Erreur lors du changement de statut de la borne`);
        },
        complete: () => {
          this.actionInProgress = false;
        }
      });
    }
  }

  getStatusBadgeClass(status: BorneStatus): string {
    const classes: Record<BorneStatus, string> = {
      'DISPONIBLE': 'badge bg-success',
      'OCCUPE': 'badge bg-warning',
      'HORS_SERVICE': 'badge bg-danger'
    };
    return classes[status];
  }
}