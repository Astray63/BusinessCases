export interface Signalement {
  idSignalement: number;
  description: string;
  statut: StatutSignalement;
  dateSignalement: Date;
  dateResolution?: Date;
  createdAt: Date;
  updatedAt: Date;

  // Informations sur l'utilisateur
  userId: number;
  userPseudo: string;
  userNom: string;
  userPrenom: string;

  // Informations sur la borne
  borneId: number;
  borneNom: string;

  // Informations sur la réservation (optionnel)
  reservationId?: number;
}

export interface CreateSignalementRequest {
  description: string;
  borneId: number;
  reservationId?: number;
}

export enum StatutSignalement {
  OUVERT = 'OUVERT',
  EN_COURS = 'EN_COURS',
  RESOLU = 'RESOLU',
  FERME = 'FERME'
}

export function getStatutLabel(statut: StatutSignalement): string {
  const labels: Record<StatutSignalement, string> = {
    [StatutSignalement.OUVERT]: 'Ouvert',
    [StatutSignalement.EN_COURS]: 'En cours',
    [StatutSignalement.RESOLU]: 'Résolu',
    [StatutSignalement.FERME]: 'Fermé'
  };
  return labels[statut];
}

export function getStatutColor(statut: StatutSignalement): string {
  const colors: Record<StatutSignalement, string> = {
    [StatutSignalement.OUVERT]: 'bg-red-100 text-red-800',
    [StatutSignalement.EN_COURS]: 'bg-yellow-100 text-yellow-800',
    [StatutSignalement.RESOLU]: 'bg-green-100 text-green-800',
    [StatutSignalement.FERME]: 'bg-gray-100 text-gray-800'
  };
  return colors[statut];
}
