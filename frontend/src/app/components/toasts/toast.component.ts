import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { NgbToastModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule, NgbToastModule],
  template: `
    <div class="fixed top-0 right-0 p-3 z-[1200]">
      <ngb-toast
        *ngFor="let toast of toastService.toasts"
        [class]="'bg-' + toast.type"
        [autohide]="true"
        [delay]="5000"
        (hidden)="toastService.remove(toast)"
      >
        <div class="flex">
          <div class="flex-1 text-white p-3">
            <strong>{{ toast.title }}</strong><br>
            {{ toast.message }}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" (click)="toastService.remove(toast)"></button>
        </div>
      </ngb-toast>
    </div>
  `
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}