import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { PageTitleService } from '../../../services/page-title.service';
import { BreadcrumbComponent } from '../components/breadcrumb/breadcrumb.component';
import { Utilisateur } from '../../../models/utilisateur.model';

@Component({
  selector: 'app-admin-utilisateurs',
  standalone: true,
  imports: [CommonModule, BreadcrumbComponent],
  template: `
    <app-breadcrumb [items]="[{ label: 'Gestion des utilisateurs' }]"></app-breadcrumb>
    
    <div class="container mt-4">
      <h2>Gestion des utilisateurs</h2>

      <div *ngIf="loading" class="text-center my-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
        <p class="mt-2">Chargement des utilisateurs...</p>
      </div>

      <div *ngIf="!loading" class="table-responsive">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nom</th>
              <th>Prénom</th>
              <th>Email</th>
              <th>Pseudo</th>
              <th>Rôle</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let user of utilisateurs">
              <td>{{user.idUtilisateur}}</td>
              <td>{{user.nom}}</td>
              <td>{{user.prenom}}</td>
              <td>{{user.email}}</td>
              <td>{{user.pseudo}}</td>
              <td>
                <span [class]="getRoleBadgeClass(user.role)">
                  {{user.role}}
                </span>
              </td>
              <td>
                <span [class]="user.actif ? 'badge bg-success' : 'badge bg-danger'">
                  {{user.actif ? 'Actif' : 'Inactif'}}
                </span>
              </td>
              <td>
                <div class="btn-group" [class.disabled]="actionInProgress">
                  <button 
                    class="btn btn-sm btn-primary me-2" 
                    (click)="editUser(user)"
                    [disabled]="actionInProgress">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button 
                    class="btn btn-sm" 
                    [class.btn-danger]="user.actif"
                    [class.btn-success]="!user.actif"
                    (click)="toggleUserStatus(user)"
                    [disabled]="actionInProgress">
                    <i class="bi" 
                       [class.bi-person-x]="user.actif"
                       [class.bi-person-check]="!user.actif">
                    </i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div *ngIf="utilisateurs.length === 0" class="text-center my-5">
          <p>Aucun utilisateur trouvé</p>
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
export class UtilisateursAdminComponent implements OnInit, OnDestroy {
  utilisateurs: Utilisateur[] = [];
  loading = false;
  actionInProgress = false;

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    private pageTitleService: PageTitleService
  ) {}

  ngOnInit(): void {
    this.pageTitleService.setTitle('Gestion des utilisateurs');
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.pageTitleService.setTitle('');
  }

  loadUsers(): void {
    this.loading = true;
    this.authService.getAllUsers().subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.utilisateurs = response.data ?? [];
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement des utilisateurs:', error);
        this.toastService.showError('Erreur lors du chargement des utilisateurs');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  editUser(user: Utilisateur): void {
    // TODO: Implémenter la logique de modification
    console.log('Modification de l\'utilisateur:', user);
    this.toastService.showWarning('La modification des utilisateurs n\'est pas encore implémentée');
  }

  toggleUserStatus(user: Utilisateur): void {
    if (!user.idUtilisateur) {
      this.toastService.showError('Identifiant utilisateur manquant');
      return;
    }

    const action = user.actif ? 'désactiver' : 'réactiver';
    if (confirm(`Êtes-vous sûr de vouloir ${action} cet utilisateur ?`)) {
      this.actionInProgress = true;
      const method = user.actif ? 'banUser' : 'unbanUser';
      this.authService[method](user.idUtilisateur).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            this.loadUsers();
            this.toastService.showSuccess(`L'utilisateur a été ${action} avec succès`);
          }
        },
        error: (error) => {
          console.error(`Erreur lors de la modification du statut de l'utilisateur:`, error);
          this.toastService.showError(`Erreur lors de la modification du statut de l'utilisateur`);
        },
        complete: () => {
          this.actionInProgress = false;
        }
      });
    }
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'badge bg-primary' : 'badge bg-info';
  }
}