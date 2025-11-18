import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-0 right-0 p-4 z-[1200] space-y-3">
      <div
        *ngFor="let toast of toastService.toasts"
        [ngClass]="{
          'bg-green-600 text-white': toast.type === 'success',
          'bg-red-600 text-white': toast.type === 'danger',
          'bg-yellow-400 text-gray-900': toast.type === 'warning',
          'bg-blue-600 text-white': toast.type === 'info'
        }"
        class="min-w-[300px] max-w-md rounded-lg shadow-lg p-4 transform transition-all duration-300 ease-in-out animate-slide-in"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <strong class="block font-semibold text-lg mb-1">{{ toast.title }}</strong>
            <div class="text-sm">{{ toast.message }}</div>
          </div>
          <button
            (click)="toastService.remove(toast)"
            class="ml-4 text-current opacity-70 hover:opacity-100 transition-opacity"
            [ngClass]="{
              'text-white': toast.type !== 'warning',
              'text-gray-900': toast.type === 'warning'
            }"
          >
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slide-in {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
    .animate-slide-in {
      animation: slide-in 0.3s ease-out;
    }
  `]
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}