export interface DashboardStats {
  clientStats: ClientStats;
  ownerStats?: OwnerStats;
  recentReservations: any[];
  recentBornes?: any[];
}

export interface ClientStats {
  totalReservations: number;
  reservationsEnCours: number;
  reservationsConfirmees: number;
  reservationsTerminees: number;
  reservationsAnnulees: number;
  montantTotalDepense: number;
  montantMoisEnCours: number;
  prochaineReservation?: any;
  reservationsCeMois: number;
}

export interface OwnerStats {
  totalBornes: number;
  bornesDisponibles: number;
  bornesOccupees: number;
  bornesMaintenance: number;
  bornesHorsService: number;
  demandesEnAttente: number;
  reservationsConfirmees: number;
  revenusEstimesMois: number;
  revenusTotaux: number;
  totalReservations: number;
  borneLaPlusReservee?: any;
  tauxOccupationMoyen: number;
}
