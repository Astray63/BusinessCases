import { Injectable } from '@angular/core';
import { Reservation, ReservationBackend, ReservationStatus } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationMapperService {

  constructor() { }

  /**
   * Mappe le modèle de réservation backend vers le modèle frontend
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
   * Mappe un tableau de réservations backend vers les modèles frontend
   */
  mapArrayToFrontend(backendArray: ReservationBackend[]): Reservation[] {
    return backendArray.map(item => this.mapToFrontend(item));
  }

  /**
   * Mappe le statut backend (état) vers le statut frontend
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
   * Filtre les réservations par statut
   */
  filterByStatus(reservations: Reservation[], status: string): Reservation[] {
    return reservations.filter(r => r.statut === status);
  }

  /**
   * Filtre les réservations en attente
   */
  filterPending(reservations: Reservation[]): Reservation[] {
    return this.filterByStatus(reservations, 'EN_ATTENTE');
  }

  /**
   * Filtre les réservations non en attente (historique)
   */
  filterNonPending(reservations: Reservation[]): Reservation[] {
    return reservations.filter(r => r.statut !== 'EN_ATTENTE');
  }
}
