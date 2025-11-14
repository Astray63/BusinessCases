import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { BorneService } from '../../services/borne.service';
import { Borne } from '../../models/borne.model';
import { Reservation, ReservationStatus, ReservationFiltre } from '../../models/reservation.model';
import { ToastService } from '../../services/toast.service';
import { AuthService } from '../../services/auth.service';
import { ApiResponse } from '../../models/api-response.model';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.scss']
})
export class ReservationComponent implements OnInit {
  // Forms
  reservationForm: FormGroup;
  filtreForm: FormGroup;

  // Data
  bornes: Borne[] = [];
  bornesDisponibles: Borne[] = [];
  reservations: Reservation[] = [];
  reservationsEnCours: Reservation[] = [];
  reservationsPassees: Reservation[] = [];
  reservationsProprietaire: Reservation[] = [];
  
  // UI State
  isLoading = false;
  activeTab: 'nouvelle' | 'en-cours' | 'passees' | 'proprietaire' = 'nouvelle';
  isProprietaire = false;
  selectedBorne: Borne | null = null;
  
  // Filtre
  filtreActif = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private reservationService: ReservationService,
    private borneService: BorneService,
    private toastService: ToastService,
    private authService: AuthService
  ) {
    this.reservationForm = this.fb.group({
      borneId: ['', Validators.required],
      dateDebut: ['', Validators.required],
      heureDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      heureFin: ['', Validators.required]
    });

    this.filtreForm = this.fb.group({
      statut: [''],
      dateDebut: [''],
      dateFin: [''],
      borneId: ['']
    });
  }

  ngOnInit(): void {
    this.checkIfProprietaire();
    this.loadBornes();
    this.loadReservations();
    
    // Vérifier si une borne est pré-sélectionnée depuis les query params
    this.route.queryParams.subscribe(params => {
      if (params['borneId']) {
        const borneId = parseInt(params['borneId']);
        this.reservationForm.patchValue({ borneId: borneId });
        this.preSelectBorne(borneId);
      }
    });
  }

  preSelectBorne(borneId: number): void {
    // Attendre que les bornes soient chargées
    setTimeout(() => {
      this.selectedBorne = this.bornesDisponibles.find(b => b.idBorne === borneId) || null;
    }, 500);
  }

  loadBornes(): void {
    this.isLoading = true;
    // Charger toutes les bornes pour les filtres
    this.borneService.getAllBornes().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.bornes = response.data || [];
      },
      error: (err: any) => this.toastService.showError('Erreur lors du chargement des bornes')
    });

    // Charger les bornes disponibles pour les réservations
    this.borneService.getBornesDisponibles().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.bornesDisponibles = response.data || [];
        this.isLoading = false;
      },
      error: (err: any) => {
        this.toastService.showError('Erreur lors du chargement des bornes disponibles');
        this.isLoading = false;
      }
    });
  }

  loadReservations(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.isLoading = true;

    // Charger les réservations de l'utilisateur
    this.reservationService.getReservationsByCurrentUser(currentUser.idUtilisateur).subscribe({
      next: (response: ApiResponse<Reservation[]>) => {
        this.reservations = response.data || [];
        this.categoriserReservations();
        this.isLoading = false;
      },
      error: (err: any) => {
        this.toastService.showError('Erreur lors du chargement des réservations');
        this.isLoading = false;
      }
    });

    // Si propriétaire, charger aussi les réservations de ses bornes
    if (this.isProprietaire) {
      this.reservationService.getReservationsProprietaire(currentUser.idUtilisateur).subscribe({
        next: (response: ApiResponse<Reservation[]>) => {
          this.reservationsProprietaire = response.data || [];
        },
        error: (err: any) => this.toastService.showError('Erreur lors du chargement des réservations propriétaire')
      });
    }
  }

  checkIfProprietaire(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    // Vérifier si l'utilisateur possède des bornes
    this.borneService.getBornesByUtilisateur(currentUser.idUtilisateur).subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.isProprietaire = (response.data && response.data.length > 0) || false;
      },
      error: () => this.isProprietaire = false
    });
  }

  categoriserReservations(): void {
    const maintenant = new Date();
    
    this.reservationsEnCours = this.reservations.filter(r => 
      (r.statut === 'EN_ATTENTE' || r.statut === 'CONFIRMEE') && 
      new Date(r.dateFin) >= maintenant
    );
    
    this.reservationsPassees = this.reservations.filter(r => 
      r.statut === 'TERMINEE' || r.statut === 'ANNULEE' || r.statut === 'REFUSEE' ||
      (r.statut === 'CONFIRMEE' && new Date(r.dateFin) < maintenant)
    );
  }

  // Créer une nouvelle réservation
  onSubmit(): void {
    if (this.reservationForm.invalid) {
      this.toastService.showError('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.isLoading = true;
    const formValues = this.reservationForm.value;
    const currentUser = this.authService.getCurrentUser();
    
    if (!currentUser) {
      this.toastService.showError('Utilisateur non authentifié');
      this.isLoading = false;
      return;
    }

    const dateDebut = new Date(`${formValues.dateDebut}T${formValues.heureDebut}`);
    const dateFin = new Date(`${formValues.dateFin}T${formValues.heureFin}`);

    // Validation des dates
    if (dateDebut >= dateFin) {
      this.toastService.showError('La date de fin doit être après la date de début');
      this.isLoading = false;
      return;
    }

    if (dateDebut < new Date()) {
      this.toastService.showError('Impossible de réserver dans le passé');
      this.isLoading = false;
      return;
    }

    const reservationPayload: any = {
      utilisateurId: currentUser.idUtilisateur,
      chargingStationId: formValues.borneId,
      dateDebut: dateDebut.toISOString(),
      dateFin: dateFin.toISOString()
    };
    
    this.reservationService.createReservation(reservationPayload).subscribe({
      next: (response: ApiResponse<Reservation>) => {
        this.toastService.showSuccess(response.message || 'Réservation créée avec succès ! Le propriétaire a été notifié.');
        this.reservationForm.reset();
        this.loadReservations();
        this.isLoading = false;
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors de la création de la réservation';
        this.toastService.showError(errorMsg);
        this.isLoading = false;
      }
    });
  }

  // Annuler une réservation
  cancelReservation(reservationId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) {
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.reservationService.cancelReservation(reservationId, currentUser.idUtilisateur).subscribe({
      next: (response: ApiResponse<any>) => {
        this.toastService.showSuccess(response.message || 'Réservation annulée avec succès');
        this.loadReservations();
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors de l\'annulation';
        this.toastService.showError(errorMsg);
      }
    });
  }

  // Accepter une réservation (propriétaire)
  accepterReservation(reservationId: number): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.reservationService.accepterReservation(reservationId, currentUser.idUtilisateur).subscribe({
      next: (response: ApiResponse<Reservation>) => {
        this.toastService.showSuccess('Réservation acceptée avec succès');
        this.loadReservations();
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors de l\'acceptation';
        this.toastService.showError(errorMsg);
      }
    });
  }

  // Refuser une réservation (propriétaire)
  refuserReservation(reservationId: number): void {
    const motif = prompt('Motif du refus (optionnel) :');
    
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.reservationService.refuserReservation(reservationId, currentUser.idUtilisateur, motif || undefined).subscribe({
      next: (response: ApiResponse<Reservation>) => {
        this.toastService.showSuccess('Réservation refusée');
        this.loadReservations();
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors du refus';
        this.toastService.showError(errorMsg);
      }
    });
  }

  // Appliquer les filtres
  appliquerFiltre(): void {
    const filtres = this.filtreForm.value;
    const filtre: ReservationFiltre = {};

    if (filtres.statut) filtre.statut = filtres.statut;
    if (filtres.dateDebut) filtre.dateDebut = new Date(filtres.dateDebut);
    if (filtres.dateFin) filtre.dateFin = new Date(filtres.dateFin);
    if (filtres.borneId) filtre.borneId = parseInt(filtres.borneId);

    this.reservationService.getReservationsFiltrees(filtre).subscribe({
      next: (response: ApiResponse<Reservation[]>) => {
        this.reservationsPassees = response.data || [];
        this.filtreActif = true;
        this.toastService.showSuccess('Filtres appliqués');
      },
      error: (err: any) => this.toastService.showError('Erreur lors de l\'application des filtres')
    });
  }

  // Réinitialiser les filtres
  reinitialiserFiltre(): void {
    this.filtreForm.reset();
    this.filtreActif = false;
    this.categoriserReservations();
  }

  // Exporter les réservations en Excel
  exporterExcel(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.isLoading = true;

    const exportData = this.reservationsPassees.map(r => ({
      'ID': r.idReservation,
      'Borne': `${r.borne.localisation}`,
      'Client': `${r.utilisateur.prenom} ${r.utilisateur.nom}`,
      'Date début': new Date(r.dateDebut).toLocaleString('fr-FR'),
      'Date fin': new Date(r.dateFin).toLocaleString('fr-FR'),
      'Statut': this.getStatutLabel(r.statut),
      'Montant': r.montantTotal ? `${r.montantTotal} €` : 'N/A'
    }));

    const ws = XLSX.utils.json_to_sheet(exportData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Réservations');
    
    const fileName = `reservations_${new Date().toISOString().split('T')[0]}.xlsx`;
    XLSX.writeFile(wb, fileName);
    
    this.toastService.showSuccess('Export Excel réussi');
    this.isLoading = false;
  }

  // Générer un reçu PDF
  genererRecuPDF(reservation: Reservation): void {
    this.isLoading = true;
    
    this.reservationService.genererRecuPDF(reservation.idReservation).subscribe({
      next: (blob: Blob) => {
        saveAs(blob, `recu_reservation_${reservation.idReservation}.pdf`);
        this.toastService.showSuccess('Reçu PDF généré avec succès');
        this.isLoading = false;
      },
      error: (err: any) => {
        // Génération locale si le backend ne le supporte pas encore
        this.genererRecuPDFLocal(reservation);
        this.isLoading = false;
      }
    });
  }

  // Générer un reçu PDF localement avec jsPDF
  genererRecuPDFLocal(reservation: Reservation): void {
    const doc = new jsPDF();
    
    // En-tête
    doc.setFontSize(20);
    doc.text('REÇU DE RÉSERVATION', 105, 20, { align: 'center' });
    
    // Informations de réservation
    doc.setFontSize(12);
    doc.text(`Numéro de réservation : ${reservation.idReservation}`, 20, 40);
    doc.text(`Date d'émission : ${new Date().toLocaleDateString('fr-FR')}`, 20, 50);
    
    // Séparateur
    doc.line(20, 55, 190, 55);
    
    // Détails client
    doc.setFontSize(14);
    doc.text('Client', 20, 65);
    doc.setFontSize(11);
    doc.text(`${reservation.utilisateur.prenom} ${reservation.utilisateur.nom}`, 20, 73);
    doc.text(`Email : ${reservation.utilisateur.email}`, 20, 80);
    
    // Détails de la borne
    doc.setFontSize(14);
    doc.text('Borne de recharge', 20, 95);
    doc.setFontSize(11);
    doc.text(`Localisation : ${reservation.borne.localisation}`, 20, 103);
    doc.text(`Type : ${reservation.borne.type}`, 20, 110);
    doc.text(`Puissance : ${reservation.borne.puissance} kW`, 20, 117);
    
    // Période de réservation
    doc.setFontSize(14);
    doc.text('Période de réservation', 20, 132);
    doc.setFontSize(11);
    doc.text(`Début : ${new Date(reservation.dateDebut).toLocaleString('fr-FR')}`, 20, 140);
    doc.text(`Fin : ${new Date(reservation.dateFin).toLocaleString('fr-FR')}`, 20, 147);
    
    // Calcul de la durée
    const duree = (new Date(reservation.dateFin).getTime() - new Date(reservation.dateDebut).getTime()) / (1000 * 60 * 60);
    doc.text(`Durée : ${duree.toFixed(2)} heures`, 20, 154);
    
    // Montant
    doc.setFontSize(14);
    doc.text('Montant', 20, 169);
    doc.setFontSize(11);
    const montant = reservation.montantTotal || (duree * reservation.borne.prix);
    doc.text(`Total : ${montant.toFixed(2)} €`, 20, 177);
    
    // Statut
    doc.setFontSize(11);
    doc.text(`Statut : ${this.getStatutLabel(reservation.statut)}`, 20, 190);
    
    // Pied de page
    doc.setFontSize(9);
    doc.text('Merci pour votre réservation !', 105, 270, { align: 'center' });
    doc.text('Electric Charge - Service de réservation de bornes électriques', 105, 280, { align: 'center' });
    
    // Télécharger le PDF
    doc.save(`recu_reservation_${reservation.idReservation}.pdf`);
    this.toastService.showSuccess('Reçu PDF généré localement');
  }

  // Changer d'onglet
  changerTab(tab: 'nouvelle' | 'en-cours' | 'passees' | 'proprietaire'): void {
    this.activeTab = tab;
  }

  // Sélectionner une borne
  selectionnerBorne(event: any): void {
    const borneId = parseInt(event.target.value);
    this.selectedBorne = this.bornesDisponibles.find(b => b.idBorne === borneId) || null;
  }

  // Utilitaires
  getStatutLabel(statut: ReservationStatus): string {
    const labels: Record<ReservationStatus, string> = {
      'EN_ATTENTE': 'En attente',
      'CONFIRMEE': 'Confirmée',
      'ANNULEE': 'Annulée',
      'TERMINEE': 'Terminée',
      'REFUSEE': 'Refusée'
    };
    return labels[statut] || statut;
  }

  getStatutClass(statut: ReservationStatus): string {
    const classes: Record<ReservationStatus, string> = {
      'EN_ATTENTE': 'statut-attente',
      'CONFIRMEE': 'statut-confirmee',
      'ANNULEE': 'statut-annulee',
      'TERMINEE': 'statut-terminee',
      'REFUSEE': 'statut-refusee'
    };
    return classes[statut] || '';
  }

  getBorneName(borneId: number): string {
    const borne = this.bornes.find(b => b.idBorne === borneId);
    return borne ? `${borne.localisation}` : 'Borne inconnue';
  }

  calculerDuree(dateDebut: Date, dateFin: Date): string {
    const dureeMs = new Date(dateFin).getTime() - new Date(dateDebut).getTime();
    const heures = Math.floor(dureeMs / (1000 * 60 * 60));
    const minutes = Math.floor((dureeMs % (1000 * 60 * 60)) / (1000 * 60));
    return `${heures}h${minutes > 0 ? minutes + 'min' : ''}`;
  }

  getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }
}