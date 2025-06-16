import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ErrorBoundaryComponent } from './error-boundary/error-boundary.component';
import { LoadingSpinnerComponent } from '../../components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, ErrorBoundaryComponent, LoadingSpinnerComponent],
  template: `
    <app-loading-spinner></app-loading-spinner>
    
    <div class="admin-container">
      <nav class="admin-sidebar">
        <div class="sidebar-header">
          <h3>Administration</h3>
        </div>
        <ul class="nav flex-column">
          <li class="nav-item">
            <a class="nav-link" routerLink="./dashboard" routerLinkActive="active">
              <i class="bi bi-speedometer2 me-2"></i>
              Tableau de bord
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="./bornes" routerLinkActive="active">
              <i class="bi bi-ev-station me-2"></i>
              Gestion des bornes
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="./utilisateurs" routerLinkActive="active">
              <i class="bi bi-people me-2"></i>
              Gestion des utilisateurs
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="./reservations" routerLinkActive="active">
              <i class="bi bi-calendar-check me-2"></i>
              Gestion des réservations
            </a>
          </li>
        </ul>
        <div class="sidebar-footer">
          <button class="btn btn-outline-light" (click)="logout()">
            <i class="bi bi-box-arrow-right me-2"></i>
            Se déconnecter
          </button>
        </div>
      </nav>
      
      <main class="admin-content">
        <app-error-boundary [componentName]="'Admin Layout'">
          <router-outlet></router-outlet>
        </app-error-boundary>
      </main>
    </div>
  `,
  styles: [`
    .admin-container {
      display: flex;
      min-height: 100vh;
    }

    .admin-sidebar {
      width: 280px;
      background-color: #343a40;
      color: white;
      padding: 1rem;
      display: flex;
      flex-direction: column;
    }

    .sidebar-header {
      padding: 1rem 0;
      border-bottom: 1px solid rgba(255,255,255,0.1);
      margin-bottom: 1rem;
    }

    .nav-link {
      color: rgba(255,255,255,0.75);
      padding: 0.75rem 1rem;
      margin: 0.2rem 0;
      border-radius: 4px;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;

      &:hover, &.active {
        color: white;
        background-color: rgba(255,255,255,0.1);
      }

      i {
        font-size: 1.1rem;
      }
    }

    .sidebar-footer {
      margin-top: auto;
      padding-top: 1rem;
      border-top: 1px solid rgba(255,255,255,0.1);
    }

    .admin-content {
      flex: 1;
      padding: 2rem;
      background-color: #f8f9fa;
      overflow-y: auto;
    }

    .btn-outline-light {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }
  `]
})
export class AdminComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}