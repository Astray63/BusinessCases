import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Borne } from '../../models/borne.model';

@Component({
  selector: 'app-borne-card',
  templateUrl: './borne-card.component.html'
})
export class BorneCardComponent {
  @Input() borne!: Borne;
  @Input() userLocation: { lat: number; lng: number } | null = null;
  @Input() showDistance: boolean = true;
  
  @Output() reserve = new EventEmitter<Borne>();
  @Output() viewOnMap = new EventEmitter<Borne>();
  @Output() viewDetails = new EventEmitter<Borne>();

  onReserve(event?: Event): void {
    if (event) event.stopPropagation();
    this.reserve.emit(this.borne);
  }

  onViewOnMap(event?: Event): void {
    if (event) event.stopPropagation();
    this.viewOnMap.emit(this.borne);
  }

  onViewDetails(event?: Event): void {
    if (event) event.stopPropagation();
    this.viewDetails.emit(this.borne);
  }

  calculateDistance(): number {
    if (!this.userLocation || !this.borne.latitude || !this.borne.longitude) {
      return 0;
    }

    const R = 6371; // Rayon de la Terre en km
    const dLat = this.deg2rad(this.borne.latitude - this.userLocation.lat);
    const dLon = this.deg2rad(this.borne.longitude - this.userLocation.lng);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(this.userLocation.lat)) *
      Math.cos(this.deg2rad(this.borne.latitude)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    return distance;
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  getEtatLabel(etat: string): string {
    switch(etat) {
      case 'DISPONIBLE': return 'Disponible';
      case 'OCCUPE': return 'Occupée';
      case 'MAINTENANCE': return 'En maintenance';
      case 'HORS_SERVICE': return 'Hors service';
      default: return etat;
    }
  }
}
