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
  borneId: number;
  borneNom: string;
}

export interface CreateAvisRequest {
  note: number;
  commentaire?: string;
  borneId: number;
}

export interface AvisMoyenne {
  moyenne: number;
  nombreAvis: number;
}
