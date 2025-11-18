export type BorneType = 'NORMALE' | 'RAPIDE';
export type BorneStatus = 'DISPONIBLE' | 'OCCUPE' | 'HORS_SERVICE' | 'MAINTENANCE';

export interface Borne {
  idBorne?: number;
  id?: number; // Backend uses 'id'
  numero?: string;
  nom?: string;
  localisation: string;
  type: BorneType;
  puissance: number;
  etat: BorneStatus;
  prix?: number;
  prixALaMinute?: number; // Backend uses this
  latitude: number;
  longitude: number;
  dateCreation?: Date;
  dateModification?: Date;
  medias?: string[];
  instructionSurPied?: string;
  instruction?: string; // Instructions d'accès
  surPied?: boolean; // Si la borne est sur pied
  ville?: string; // Ville
  connectorType?: string;
  description?: string;
  occupee?: boolean;
  ownerId?: number;
  address?: string;
  hourlyRate?: number;
}

export interface BorneFiltre {
  latitude?: number;
  longitude?: number;
  distanceMax?: number;
  tarifMin?: number;
  tarifMax?: number;
  puissanceMin?: number;
  type?: BorneType;
}