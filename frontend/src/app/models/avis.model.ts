export interface Avis {
  idAvis: number;
  note: number;
  commentaire?: string;
  createdAt: Date;
  updatedAt: Date;
  
  // Informations sur l'utilisateur
  utilisateurId: number;
  utilisateurPseudo: string;
  utilisateurNom: string;
  utilisateurPrenom: string;
  
  // Informations sur la borne
  chargingStationId: number;
  chargingStationNom: string;
}

export interface CreateAvisRequest {
  note: number;
  commentaire?: string;
  chargingStationId: number;
}

export interface AvisMoyenne {
  moyenne: number;
  nombreAvis: number;
}
