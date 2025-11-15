import { Utilisateur } from './utilisateur.model';
import { Borne } from './borne.model';

export type ReservationStatus = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE' | 'TERMINEE' | 'REFUSEE' | 'ACTIVE';

// Backend response format
export interface ReservationBackend {
  id: number;
  utilisateurId: number;
  chargingStationId: number;
  dateDebut: string;
  dateFin: string;
  etat: string;
  prixALaMinute: number;
  totalPrice: number;
  borne?: Borne;
  utilisateur?: Utilisateur;
}

// Frontend model
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
  notificationEnvoyee?: boolean;
  motifRefus?: string;
}

export interface ReservationFiltre {
  statut?: ReservationStatus;
  dateDebut?: Date;
  dateFin?: Date;
  borneId?: number;
  utilisateurId?: number;
}

export interface ReservationExportParams {
  userId?: number;
  borneId?: number;
  statut?: ReservationStatus;
  dateDebut?: Date;
  dateFin?: Date;
}