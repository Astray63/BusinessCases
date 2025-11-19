import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SignalementService } from '../../services/signalement.service';
import { Signalement, CreateSignalementRequest, StatutSignalement, getStatutLabel, getStatutColor } from '../../models/signalement.model';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-signalement-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './signalement-form.component.html'
})
export class SignalementFormComponent implements OnInit {
  @Input() chargingStationId!: number;
  @Input() chargingStationNom!: string;

  signalements: Signalement[] = [];
  loading = false;
  showForm = false;
  
  // Form data
  newSignalement: CreateSignalementRequest = {
    description: '',
    chargingStationId: 0
  };

  currentUserId: number | null = null;

  constructor(
    private signalementService: SignalementService,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.currentUserId = user?.idUtilisateur || null;
    this.loadSignalements();
  }

  loadSignalements(): void {
    this.loading = true;
    this.signalementService.getSignalementsByChargingStation(this.chargingStationId).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.signalements = response.data || [];
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des signalements:', error);
        this.toastService.showError('Erreur lors du chargement des signalements');
        this.loading = false;
      }
    });
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.newSignalement = {
        description: '',
        chargingStationId: this.chargingStationId
      };
    }
  }

  submitSignalement(): void {
    if (!this.newSignalement.description || this.newSignalement.description.trim().length === 0) {
      this.toastService.showError('Veuillez décrire le problème rencontré');
      return;
    }

    this.newSignalement.chargingStationId = this.chargingStationId;
    this.loading = true;

    this.signalementService.createSignalement(this.newSignalement).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Signalement envoyé avec succès');
          this.showForm = false;
          this.loadSignalements();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors de la création du signalement:', error);
        this.toastService.showError(error.error?.message || 'Erreur lors de la création du signalement');
        this.loading = false;
      }
    });
  }

  deleteSignalement(signalementId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce signalement ?')) {
      return;
    }

    this.signalementService.deleteSignalement(signalementId).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Signalement supprimé avec succès');
          this.loadSignalements();
        }
      },
      error: (error) => {
        console.error('Erreur lors de la suppression du signalement:', error);
        this.toastService.showError('Erreur lors de la suppression du signalement');
      }
    });
  }

  getStatutLabel(statut: StatutSignalement): string {
    return getStatutLabel(statut);
  }

  getStatutColor(statut: StatutSignalement): string {
    return getStatutColor(statut);
  }

  canDeleteSignalement(signalement: Signalement): boolean {
    return this.currentUserId === signalement.userId;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
