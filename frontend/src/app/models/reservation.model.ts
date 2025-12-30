import { Utilisateur } from './utilisateur.model';
import { Borne } from './borne.model';

export type ReservationStatus = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE' | 'TERMINEE' | 'REFUSEE' | 'ACTIVE';

// Format de la réponse backend
export interface ReservationBackend {
  id: number;
  utilisateurId: number;
  borneId: number;
  dateDebut: string;
  dateFin: string;
  etat: string;
  prixALaMinute: number;
  totalPrice: number;
  receiptPath?: string;
  borne?: Borne;
  utilisateur?: Utilisateur;
}

// Modèle frontend
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
  receiptPath?: string;
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