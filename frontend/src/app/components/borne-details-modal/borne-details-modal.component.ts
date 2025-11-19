import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Borne } from '../../models/borne.model';
import { AvisListComponent } from '../avis-list/avis-list.component';
import { SignalementFormComponent } from '../signalement-form/signalement-form.component';

@Component({
  selector: 'app-borne-details-modal',
  standalone: true,
  imports: [CommonModule, AvisListComponent, SignalementFormComponent],
  templateUrl: './borne-details-modal.component.html'
})
export class BorneDetailsModalComponent {
  @Input() borne: Borne | null = null;
  @Input() isOpen = false;
  @Output() closeModal = new EventEmitter<void>();

  activeTab: 'details' | 'avis' | 'signalements' = 'details';

  close(): void {
    this.closeModal.emit();
  }

  setActiveTab(tab: 'details' | 'avis' | 'signalements'): void {
    this.activeTab = tab;
  }

  getEtatColor(etat: string): string {
    const colors: Record<string, string> = {
      'DISPONIBLE': 'bg-green-100 text-green-800',
      'OCCUPE': 'bg-yellow-100 text-yellow-800',
      'HORS_SERVICE': 'bg-red-100 text-red-800',
      'MAINTENANCE': 'bg-orange-100 text-orange-800'
    };
    return colors[etat] || 'bg-gray-100 text-gray-800';
  }
}
