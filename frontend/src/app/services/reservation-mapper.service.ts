import { Injectable } from '@angular/core';
import { Reservation, ReservationBackend, ReservationStatus } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationMapperService {

  constructor() {}

  /**
   * Maps backend reservation model to frontend model
   */
  mapToFrontend(backend: ReservationBackend): Reservation {
    return {
      idReservation: backend.id,
      utilisateur: backend.utilisateur!,
      borne: backend.borne!,
      dateDebut: new Date(backend.dateDebut),
      dateFin: new Date(backend.dateFin),
      statut: this.mapEtatToStatut(backend.etat),
      montantTotal: backend.totalPrice,
      receiptPath: backend.receiptPath
    };
  }

  /**
   * Maps an array of backend reservations to frontend models
   */
  mapArrayToFrontend(backendArray: ReservationBackend[]): Reservation[] {
    return backendArray.map(item => this.mapToFrontend(item));
  }

  /**
   * Maps backend status (etat) to frontend status (statut)
   */
  private mapEtatToStatut(etat: string): ReservationStatus {
    const mapping: { [key: string]: ReservationStatus } = {
      'ACTIVE': 'CONFIRMEE',
      'TERMINEE': 'TERMINEE',
      'ANNULEE': 'ANNULEE',
      'CONFIRMEE': 'CONFIRMEE',
      'EN_ATTENTE': 'EN_ATTENTE',
      'REFUSEE': 'REFUSEE'
    };
    return mapping[etat] || 'EN_ATTENTE';
  }

  /**
   * Filter reservations by status
   */
  filterByStatus(reservations: Reservation[], status: string): Reservation[] {
    return reservations.filter(r => r.statut === status);
  }

  /**
   * Filter pending reservations
   */
  filterPending(reservations: Reservation[]): Reservation[] {
    return this.filterByStatus(reservations, 'EN_ATTENTE');
  }

  /**
   * Filter non-pending reservations (history)
   */
  filterNonPending(reservations: Reservation[]): Reservation[] {
    return reservations.filter(r => r.statut !== 'EN_ATTENTE');
  }
}
