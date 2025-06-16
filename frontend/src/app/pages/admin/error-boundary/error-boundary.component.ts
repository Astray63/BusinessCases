import { Component, ErrorHandler, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-error-boundary',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="error" class="error-container">
      <div class="error-content">
        <i class="bi bi-exclamation-triangle-fill text-danger"></i>
        <h3>Une erreur est survenue</h3>
        <p>{{ errorMessage }}</p>
        <button class="btn btn-primary" (click)="retry()">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Réessayer
        </button>
      </div>
    </div>
    <ng-content *ngIf="!error"></ng-content>
  `,
  styles: [`
    .error-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 200px;
      padding: 2rem;
    }

    .error-content {
      text-align: center;
      
      i {
        font-size: 3rem;
        margin-bottom: 1rem;
      }

      h3 {
        margin-bottom: 1rem;
      }

      p {
        color: #666;
        margin-bottom: 1.5rem;
      }
    }
  `]
})
export class ErrorBoundaryComponent implements ErrorHandler {
  @Input() componentName?: string;
  error: Error | null = null;
  errorMessage = 'Une erreur inattendue est survenue. Veuillez réessayer.';

  constructor(private toastService: ToastService) {}

  handleError(error: Error): void {
    console.error('Error in', this.componentName || 'component:', error);
    this.error = error;
    this.errorMessage = error.message || this.errorMessage;
    this.toastService.showError(this.errorMessage);
  }

  retry(): void {
    this.error = null;
    window.location.reload();
  }
}