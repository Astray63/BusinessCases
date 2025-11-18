import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserContextService } from '../../services/user-context.service';
import { DashboardService } from '../../services/dashboard.service';
import { ReservationService } from '../../services/reservation.service';
import { BorneService } from '../../services/borne.service';
import { Utilisateur } from '../../models/utilisateur.model';
import { DashboardStats } from '../../models/dashboard-stats.model';
import { Reservation } from '../../models/reservation.model';
import { Borne } from '../../models/borne.model';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  isProprietaire = false;
  nombreBornes = 0;
  loading = true;
  dashboardStats: DashboardStats | null = null;
  
  // Données pour le tableau
  activeReservations: any[] = [];
  pastReservations: any[] = [];
  allPastReservations: any[] = [];
  recentBornes: any[] = [];
  pendingRequests: any[] = [];
  processedRequests: any[] = [];
  allProcessedRequests: any[] = [];
  
  // Filtres
  filterDateDebut: string = '';
  filterDateFin: string = '';
  
  // Pagination pour réservations passées
  currentPagePast = 1;
  pageSizePast = 5;
  totalPagesPast = 1;
  
  // Pagination pour demandes traitées
  currentPageProcessed = 1;
  pageSizeProcessed = 5;
  totalPagesProcessed = 1;

  constructor(
    private authService: AuthService,
    private userContextService: UserContextService,
    private dashboardService: DashboardService,
    private reservationService: ReservationService,
    private borneService: BorneService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Récupérer l'utilisateur courant
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      // Charger les statistiques du dashboard
      this.loadDashboardData();
    });

    // Écouter le statut propriétaire
    this.userContextService.isProprietaire$.subscribe(isProprietaire => {
      this.isProprietaire = isProprietaire;
    });

    this.userContextService.nombreBornes$.subscribe(nombreBornes => {
      this.nombreBornes = nombreBornes;
    });
  }

  loadDashboardData(): void {
    if (!this.currentUser) return;
    
    this.loading = true;
    
    this.dashboardService.getDashboardStats(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.result === 'SUCCESS' && response.data) {
          this.dashboardStats = response.data;
          
          // Extraire les données pour les tableaux
          this.processReservations();
          
          // Mettre à jour le contexte si l'utilisateur est propriétaire
          if (this.dashboardStats.ownerStats) {
            this.isProprietaire = true;
            this.nombreBornes = this.dashboardStats.ownerStats.totalBornes;
            
            // Charger les vraies bornes du propriétaire
            this.loadOwnerBornes();
            
            // Charger les demandes de réservation
            this.loadOwnerRequests();
          }
        }
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }
  
  loadOwnerBornes(): void {
    if (!this.currentUser) return;
    
    this.borneService.getBornesByProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          // Prendre les 2 bornes les plus récentes
          this.recentBornes = response.data
            .sort((a, b) => (b.idBorne || 0) - (a.idBorne || 0))
            .slice(0, 2);
        }
      },
      error: (error) => {
      }
    });
  }
  
  processReservations(): void {
    if (!this.dashboardStats?.recentReservations) return;
    
    const now = new Date();
    
    // Séparer les réservations actives et passées
    this.activeReservations = this.dashboardStats.recentReservations.filter(r => {
      const dateDebut = new Date(r.dateDebut);
      const dateFin = new Date(r.dateFin);
      return r.statut === 'CONFIRMEE' && dateDebut <= now && dateFin >= now;
    }).slice(0, 5);
    
    // Stocker toutes les réservations passées pour la pagination
    this.allPastReservations = this.dashboardStats.recentReservations.filter(r => {
      const dateFin = new Date(r.dateFin);
      return r.statut === 'TERMINEE' || r.statut === 'ANNULEE' || r.statut === 'REFUSEE' || dateFin < now;
    });
    
    // Calculer la pagination
    this.totalPagesPast = Math.ceil(this.allPastReservations.length / this.pageSizePast);
    this.updatePastReservationsPage();
  }
  
  updatePastReservationsPage(): void {
    const start = (this.currentPagePast - 1) * this.pageSizePast;
    const end = start + this.pageSizePast;
    this.pastReservations = this.allPastReservations.slice(start, end);
  }
  
  goToPagePast(page: number): void {
    if (page >= 1 && page <= this.totalPagesPast) {
      this.currentPagePast = page;
      this.updatePastReservationsPage();
    }
  }
  
  nextPagePast(): void {
    this.goToPagePast(this.currentPagePast + 1);
  }
  
  previousPagePast(): void {
    this.goToPagePast(this.currentPagePast - 1);
  }
  
  firstPagePast(): void {
    this.goToPagePast(1);
  }
  
  lastPagePast(): void {
    this.goToPagePast(this.totalPagesPast);
  }
  
  loadOwnerRequests(): void {
    if (!this.currentUser || !this.currentUser.idUtilisateur) {
      return;
    }
    
    // Charger les vraies demandes depuis le backend
    this.reservationService.getReservationsProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          // Demandes en attente
          this.pendingRequests = response.data
            .filter(r => r.statut === 'EN_ATTENTE')
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime())
            .slice(0, 5);
          
          // Stocker toutes les demandes traitées pour la pagination
          this.allProcessedRequests = response.data
            .filter(r => ['CONFIRMEE', 'REFUSEE', 'TERMINEE', 'ANNULEE'].includes(r.statut))
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime());
          
          // Calculer la pagination
          this.totalPagesProcessed = Math.ceil(this.allProcessedRequests.length / this.pageSizeProcessed);
          this.updateProcessedRequestsPage();
        }
      },
      error: (error) => {
      }
    });
  }
  
  updateProcessedRequestsPage(): void {
    const start = (this.currentPageProcessed - 1) * this.pageSizeProcessed;
    const end = start + this.pageSizeProcessed;
    this.processedRequests = this.allProcessedRequests.slice(start, end);
  }
  
  goToPageProcessed(page: number): void {
    if (page >= 1 && page <= this.totalPagesProcessed) {
      this.currentPageProcessed = page;
      this.updateProcessedRequestsPage();
    }
  }
  
  nextPageProcessed(): void {
    this.goToPageProcessed(this.currentPageProcessed + 1);
  }
  
  previousPageProcessed(): void {
    this.goToPageProcessed(this.currentPageProcessed - 1);
  }
  
  firstPageProcessed(): void {
    this.goToPageProcessed(1);
  }
  
  lastPageProcessed(): void {
    this.goToPageProcessed(this.totalPagesProcessed);
  }
  
  applyFilters(): void {
    if (!this.currentUser) return;
    
    this.loading = true;
    this.reservationService.getMesReservationsClient(this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data) {
          let filtered = response.data;
          
          // Appliquer les filtres de date
          if (this.filterDateDebut) {
            const dateDebut = new Date(this.filterDateDebut);
            filtered = filtered.filter(r => new Date(r.dateDebut) >= dateDebut);
          }
          
          if (this.filterDateFin) {
            const dateFin = new Date(this.filterDateFin);
            filtered = filtered.filter(r => new Date(r.dateFin) <= dateFin);
          }
          
          // Filtrer les réservations passées
          const now = new Date();
          this.allPastReservations = filtered
            .filter(r => {
              const dateFin = new Date(r.dateFin);
              return r.statut === 'TERMINEE' || r.statut === 'ANNULEE' || dateFin < now;
            })
            .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime());
          
          // Recalculer la pagination
          this.currentPagePast = 1;
          this.totalPagesPast = Math.ceil(this.allPastReservations.length / this.pageSizePast);
          this.updatePastReservationsPage();
        }
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }

  accepterDemande(idReservation: number): void {
    if (!this.currentUser || !confirm('Accepter cette réservation ?')) return;
    
    this.loading = true;
    this.reservationService.accepterReservation(idReservation, this.currentUser.idUtilisateur).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.loadOwnerRequests();
          this.loading = false;
        }
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }

  refuserDemande(idReservation: number): void {
    const motif = prompt('Motif du refus (optionnel) :');
    if (motif === null) return; // Annulation
    
    if (!this.currentUser) return;
    
    this.loading = true;
    this.reservationService.refuserReservation(idReservation, this.currentUser.idUtilisateur, motif).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.loadOwnerRequests();
          this.loading = false;
        }
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }

  calculerDuree(dateDebut: Date, dateFin: Date): string {
    const heures = (new Date(dateFin).getTime() - new Date(dateDebut).getTime()) / (1000 * 60 * 60);
    return heures.toFixed(1) + 'h';
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'EN_ATTENTE': 'bg-yellow-100 text-yellow-800',
      'CONFIRMEE': 'bg-green-100 text-green-800',
      'ACTIVE': 'bg-green-100 text-green-800',
      'TERMINEE': 'bg-gray-100 text-gray-800',
      'ANNULEE': 'bg-red-100 text-red-800',
      'REFUSEE': 'bg-red-100 text-red-800'
    };
    return classes[statut] || 'bg-gray-100 text-gray-800';
  }

  getStatutLabel(statut: string): string {
    const labels: { [key: string]: string } = {
      'EN_ATTENTE': 'En attente',
      'CONFIRMEE': 'Confirmée',
      'ACTIVE': 'Active',
      'TERMINEE': 'Terminée',
      'ANNULEE': 'Annulée',
      'REFUSEE': 'Refusée'
    };
    return labels[statut] || statut;
  }

  downloadReceipt(idReservation: number): void {
    this.reservationService.downloadReceipt(idReservation).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recu-reservation-${idReservation}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
      }
    });
  }

  // Méthode pour exporter les réservations vers Excel
  exporterReservations(): void {
    if (!this.currentUser) return;
    
    // Vérifier s'il y a des réservations à exporter
    if (!this.allPastReservations || this.allPastReservations.length === 0) {
      alert('Aucune réservation à exporter');
      return;
    }
    
    // Préparer les données pour l'export
    const exportData = this.allPastReservations.map(r => ({
      'ID': r.idReservation,
      'Borne': r.borne ? r.borne.localisation || r.borne.nom : 'N/A',
      'Client': r.utilisateur ? `${r.utilisateur.prenom} ${r.utilisateur.nom}` : 'N/A',
      'Email': r.utilisateur ? r.utilisateur.email : 'N/A',
      'Date début': new Date(r.dateDebut).toLocaleString('fr-FR'),
      'Date fin': new Date(r.dateFin).toLocaleString('fr-FR'),
      'Statut': this.getStatutLabel(r.statut),
      'Montant Total': r.montantTotal ? `${r.montantTotal.toFixed(2)} €` : 'N/A'
    }));

    // Créer la feuille Excel
    const ws = XLSX.utils.json_to_sheet(exportData);
    
    // Ajuster la largeur des colonnes
    const colWidths = [
      { wch: 8 },  // ID
      { wch: 35 }, // Borne
      { wch: 25 }, // Client
      { wch: 30 }, // Email
      { wch: 20 }, // Date début
      { wch: 20 }, // Date fin
      { wch: 15 }, // Statut
      { wch: 15 }  // Montant Total
    ];
    ws['!cols'] = colWidths;
    
    // Créer le classeur
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Réservations');
    
    // Générer le nom du fichier avec la date du jour
    const fileName = `reservations_${new Date().toISOString().split('T')[0]}.xlsx`;
    
    // Télécharger le fichier
    XLSX.writeFile(wb, fileName);
    
  }

  // Méthodes pour gérer les bornes
  voirDetailsBorne(borne: Borne): void {
    if (borne.idBorne) {
      this.router.navigate(['/proprietaire/mes-bornes', borne.idBorne]);
    }
  }

  modifierBorne(borne: Borne): void {
    if (borne.idBorne) {
      this.router.navigate(['/proprietaire/mes-bornes', borne.idBorne, 'edit']);
    }
  }

  parametresBorne(borne: Borne): void {
    if (borne.idBorne) {
      this.router.navigate(['/proprietaire/mes-bornes', borne.idBorne, 'parametres']);
    }
  }

  supprimerBorne(borne: Borne): void {
    if (!borne.idBorne || !confirm(`Êtes-vous sûr de vouloir supprimer la borne "${borne.nom}" ?`)) {
      return;
    }

    this.loading = true;
    this.borneService.deleteBorne(borne.idBorne).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          alert('Borne supprimée avec succès');
          this.loadOwnerBornes(); // Recharger la liste
        }
        this.loading = false;
      },
      error: (error) => {
        alert('Erreur lors de la suppression de la borne');
        this.loading = false;
      }
    });
  }

  // Navigation methods
  ajouterBorne(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }

  reserverBorne(): void {
    this.router.navigate(['/client/recherche']);
  }

  voirMesBornes(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }

  voirMesReservations(): void {
    this.router.navigate(['/client/mes-reservations']);
  }

  voirDemandesReservation(): void {
    this.router.navigate(['/proprietaire/demandes']);
  }

  devenirProprietaire(): void {
    this.router.navigate(['/proprietaire/mes-bornes']);
  }
}
