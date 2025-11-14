import { Utilisateur } from './utilisateur.model';
import { Borne } from './borne.model';

export type ReservationStatus = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE' | 'TERMINEE' | 'REFUSEE';

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