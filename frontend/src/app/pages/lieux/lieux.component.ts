import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LieuService } from '../../services/lieu.service';
import { AuthService } from '../../services/auth.service';
import { Lieu } from '../../models/lieu.model';

@Component({
  selector: 'app-lieux',
  templateUrl: './lieux.component.html',
})
export class LieuxComponent implements OnInit {
  lieux: Lieu[] = [];
  lieuForm: FormGroup;
  editMode = false;
  selectedLieu: Lieu | null = null;
  loading = false;
  successMessage = '';
  errorMessage = '';
  showForm = false;

  constructor(
    private lieuService: LieuService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.lieuForm = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(100)]],
      adresse: ['', Validators.required],
      numero: ['', Validators.maxLength(20)],
      rue: [''],
      codePostal: ['', [Validators.required, Validators.maxLength(20)]],
      ville: ['', [Validators.required, Validators.maxLength(100)]],
      pays: ['France', [Validators.required, Validators.maxLength(100)]],
      region: ['', Validators.maxLength(100)],
      complementEtape: [''],
      latitude: ['', [Validators.min(-90), Validators.max(90)]],
      longitude: ['', [Validators.min(-180), Validators.max(180)]]
    });
  }

  ngOnInit(): void {
    this.loadUserLieux();
  }

  loadUserLieux(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'Vous devez être connecté pour voir vos lieux';
      return;
    }

    this.loading = true;
    this.lieuService.getByUtilisateur(currentUser.idUtilisateur).subscribe({
      next: (lieux) => {
        this.lieux = lieux;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement des lieux';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.lieuForm.invalid) {
      Object.keys(this.lieuForm.controls).forEach(key => {
        this.lieuForm.get(key)?.markAsTouched();
      });
      return;
    }

    const lieuData: Lieu = this.lieuForm.value;
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'Vous devez être connecté';
      return;
    }

    this.loading = true;
    this.clearMessages();

    if (this.editMode && this.selectedLieu?.idLieu) {
      this.lieuService.update(this.selectedLieu.idLieu, lieuData).subscribe({
        next: (updatedLieu) => {
          const index = this.lieux.findIndex(l => l.idLieu === updatedLieu.idLieu);
          if (index !== -1) {
            this.lieux[index] = updatedLieu;
          }
          this.successMessage = 'Lieu mis à jour avec succès';
          this.resetForm();
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Erreur lors de la mise à jour du lieu';
          this.loading = false;
        }
      });
    } else {
      this.lieuService.create(lieuData, currentUser.idUtilisateur).subscribe({
        next: (newLieu) => {
          this.lieux.push(newLieu);
          this.successMessage = 'Lieu créé avec succès';
          this.resetForm();
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Erreur lors de la création du lieu';
          this.loading = false;
        }
      });
    }
  }

  editLieu(lieu: Lieu): void {
    this.editMode = true;
    this.selectedLieu = lieu;
    this.showForm = true;
    this.lieuForm.patchValue(lieu);
    this.clearMessages();
  }

  deleteLieu(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce lieu ?')) {
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.lieuService.delete(id).subscribe({
      next: () => {
        this.lieux = this.lieux.filter(l => l.idLieu !== id);
        this.successMessage = 'Lieu supprimé avec succès';
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors de la suppression du lieu';
        this.loading = false;
      }
    });
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.lieuForm.reset({ pays: 'France' });
    this.editMode = false;
    this.selectedLieu = null;
    this.showForm = false;
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getAddressString(lieu: Lieu): string {
    const parts = [];
    if (lieu.numero) parts.push(lieu.numero);
    if (lieu.rue) parts.push(lieu.rue);
    if (lieu.complementEtape) parts.push(lieu.complementEtape);
    return parts.join(' ') || lieu.adresse;
  }

  getFullAddress(lieu: Lieu): string {
    return `${this.getAddressString(lieu)}, ${lieu.codePostal} ${lieu.ville}, ${lieu.pays}`;
  }
}
