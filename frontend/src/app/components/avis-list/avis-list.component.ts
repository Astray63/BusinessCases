import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AvisService } from '../../services/avis.service';
import { Avis, CreateAvisRequest } from '../../models/avis.model';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-avis-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './avis-list.component.html'
})
export class AvisListComponent implements OnInit {
  @Input() chargingStationId!: number;
  @Input() chargingStationNom!: string;

  avis: Avis[] = [];
  averageNote: number | undefined = 0;
  loading = false;
  showForm = false;
  Math = Math;
  
  // Form data
  newAvis: CreateAvisRequest = {
    note: 5,
    commentaire: '',
    chargingStationId: 0
  };

  currentUserId: number | null = null;

  constructor(
    private avisService: AvisService,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.currentUserId = user?.idUtilisateur || null;
    this.loadAvis();
    this.loadAverageNote();
  }

  loadAvis(): void {
    this.loading = true;
    this.avisService.getAvisByChargingStation(this.chargingStationId).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.avis = response.data || [];
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des avis:', error);
        this.toastService.showError('Erreur lors du chargement des avis');
        this.loading = false;
      }
    });
  }

  loadAverageNote(): void {
    this.avisService.getAverageNote(this.chargingStationId).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS' && response.data !== null) {
          this.averageNote = response.data;
        }
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la note moyenne:', error);
      }
    });
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.newAvis = {
        note: 5,
        commentaire: '',
        chargingStationId: this.chargingStationId
      };
    }
  }

  submitAvis(): void {
    if (!this.newAvis.note || this.newAvis.note < 1 || this.newAvis.note > 5) {
      this.toastService.showError('Veuillez sélectionner une note entre 1 et 5');
      return;
    }

    this.newAvis.chargingStationId = this.chargingStationId;
    this.loading = true;

    this.avisService.createAvis(this.newAvis).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Avis ajouté avec succès');
          this.showForm = false;
          this.loadAvis();
          this.loadAverageNote();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors de la création de l\'avis:', error);
        this.toastService.showError(error.error?.message || 'Erreur lors de la création de l\'avis');
        this.loading = false;
      }
    });
  }

  deleteAvis(avisId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet avis ?')) {
      return;
    }

    this.avisService.deleteAvis(avisId).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess('Avis supprimé avec succès');
          this.loadAvis();
          this.loadAverageNote();
        }
      },
      error: (error) => {
        console.error('Erreur lors de la suppression de l\'avis:', error);
        this.toastService.showError('Erreur lors de la suppression de l\'avis');
      }
    });
  }

  getStars(note: number): string[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= note ? 'full' : 'empty');
    }
    return stars;
  }

  canDeleteAvis(avis: Avis): boolean {
    return this.currentUserId === avis.utilisateurId;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}
