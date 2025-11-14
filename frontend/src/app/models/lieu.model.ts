export interface Lieu {
  idLieu?: number;
  nom: string;
  adresse: string;
  numero?: string;
  rue?: string;
  codePostal: string;
  ville: string;
  pays: string;
  region?: string;
  complementEtape?: string;
  latitude?: number;
  longitude?: number;
  createdAt?: string;
  updatedAt?: string;
}
