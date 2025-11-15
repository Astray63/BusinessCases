export type UserRole = 'user' | 'admin';

export interface Utilisateur {
  idUtilisateur: number;
  email: string;
  pseudo: string;
  nom: string;
  prenom: string;
  role: UserRole;
  actif: boolean;
  dateCreation?: Date;
  dateModification?: Date;
  dateNaissance?: Date;
  iban?: string;
  adressePhysique?: string;
  medias?: string;
  idAdresse?: number;
  
  // Flag dynamique calculé côté front selon si l'utilisateur possède des bornes
  isProprietaire?: boolean;
  nombreBornes?: number;
}

export interface UtilisateurAuth {
  pseudo: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: Utilisateur;
}
