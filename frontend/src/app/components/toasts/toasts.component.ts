import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbToastModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toasts',
  standalone: true,
  imports: [CommonModule, NgbToastModule],
  template: `
    <div class="toast-container position-fixed top-0 end-0 p-3">
      <ngb-toast
        *ngFor="let toast of toastService.toasts"
        [class]="'bg-' + toast.type"
        [autohide]="true"
        [delay]="5000"
        (hidden)="toastService.remove(toast)"
      >
        <div class="toast-body text-white">
          <strong>{{ toast.title }}</strong>
          <div>{{ toast.message }}</div>
        </div>
      </ngb-toast>
    </div>
  `,
  styles: [`
    .toast-container {
      z-index: 1200;
    }
    .toast-body {
      padding: 0.75rem;
    }
  `]
})
export class ToastsComponent {
  constructor(public toastService: ToastService) {}
}