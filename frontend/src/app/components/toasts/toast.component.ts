import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { NgbToastModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-toast',
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
        <div class="d-flex">
          <div class="toast-body text-white">
            <strong>{{ toast.title }}</strong><br>
            {{ toast.message }}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" (click)="toastService.remove(toast)"></button>
        </div>
      </ngb-toast>
    </div>
  `,
  styles: [`
    .toast-container {
      z-index: 1200;
    }
  `]
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}