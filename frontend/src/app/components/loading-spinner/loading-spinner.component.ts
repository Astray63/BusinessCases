import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingService } from '../../services/loading.service';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (loadingService.loading$ | async) {
      <div class="fixed top-0 left-0 w-full h-full bg-white/70 flex justify-center items-center z-[1000]">
        <div class="w-12 h-12 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin" role="status">
          <span class="sr-only">Chargement...</span>
        </div>
      </div>
    }
  `
})
export class LoadingSpinnerComponent {
  constructor(public loadingService: LoadingService) {}
}
