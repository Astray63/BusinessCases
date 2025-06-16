import { Utilisateur } from './utilisateur.model';
import { Borne } from './borne.model';

export type ReservationStatus = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE' | 'TERMINEE';

export interface Reservation {
  idReservation: number;
  utilisateur: Utilisateur;
  borne: Borne;
  dateDebut: Date;
  dateFin: Date;
  statut: ReservationStatus;
  dateCreation?: Date;
  dateModification?: Date;
  montantTotal?: number;
}