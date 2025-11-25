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
  telephone?: string;
  codePostal?: string;
  ville?: string;
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

export interface RegisterRequest {
  utilisateur: {
    nom: string;
    prenom: string;
    pseudo: string;
    email: string;
    dateNaissance: Date;
    role: string;
    iban?: string;
    adressePhysique?: string;
    telephone?: string;
    codePostal?: string;
    ville?: string;
    medias?: string;
  };
  motDePasse: string;
}

export interface AuthResponse {
  token: string;
  user: Utilisateur;
}
