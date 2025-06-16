export type BorneType = 'NORMALE' | 'RAPIDE';
export type BorneStatus = 'DISPONIBLE' | 'OCCUPE' | 'HORS_SERVICE';

export interface Borne {
  idBorne: number;
  localisation: string;
  type: BorneType;
  puissance: number;
  etat: BorneStatus;
  prix: number;
  latitude: number;
  longitude: number;
  dateCreation?: Date;
  dateModification?: Date;
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