import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BorneService } from '../../../services/borne.service';
import { LieuService } from '../../../services/lieu.service';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { Borne } from '../../../models/borne.model';
import { Lieu } from '../../../models/lieu.model';

@Component({
  selector: 'app-borne-form',
  templateUrl: './borne-form.component.html',
  styleUrls: ['./borne-form.component.scss']
})
export class BorneFormComponent implements OnInit {
  borneForm: FormGroup;
  isEditMode = false;
  borneId: number | null = null;
  loading = false;
  pageTitle = 'Ajouter une borne de recharge';
  lieux: Lieu[] = [];

  typesConnecteur = [
    { value: 'Type 2', label: 'Type 2 (Europe)' },
    { value: 'CCS', label: 'CCS Combo' },
    { value: 'CHAdeMO', label: 'CHAdeMO' },
    { value: 'Type 3', label: 'Type 3' }
  ];

  niveauxPuissance = [
    { value: 3.7, label: '3.7 kW - Charge lente' },
    { value: 7.4, label: '7.4 kW - Charge standard' },
    { value: 11, label: '11 kW - Charge accélérée' },
    { value: 22, label: '22 kW - Charge rapide' },
    { value: 50, label: '50 kW - Charge ultra-rapide' },
    { value: 100, label: '100 kW - Charge très rapide' },
    { value: 150, label: '150 kW - Supercharge' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private borneService: BorneService,
    private lieuService: LieuService,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.borneForm = this.fb.group({
      lieuId: ['', Validators.required],
      localisation: ['', [Validators.required, Validators.maxLength(255)]],
      type: ['Type 2', Validators.required],
      puissance: [7.4, [Validators.required, Validators.min(1)]],
      etat: ['DISPONIBLE', Validators.required],
      prix: [0, [Validators.required, Validators.min(0)]],
      latitude: [''],
      longitude: ['']
    });
  }

  ngOnInit(): void {
    this.loadLieux();

    // Vérifier si c'est un mode édition
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.borneId = +params['id'];
        this.pageTitle = 'Modifier la borne de recharge';
        this.loadBorne(this.borneId);
      }
    });
  }

  loadLieux(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.lieuService.getByUtilisateur(currentUser.idUtilisateur).subscribe({
      next: (lieux: Lieu[]) => {
        this.lieux = lieux;
      },
      error: (error: any) => {
        this.toastService.showError('Erreur lors du chargement des lieux');
        console.error(error);
      }
    });
  }

  loadBorne(id: number): void {
    this.loading = true;
    this.borneService.getBorneById(id).subscribe({
      next: (response: any) => {
        const borne = response.data || response;
        this.borneForm.patchValue(borne);
        this.loading = false;
      },
      error: (error: any) => {
        this.toastService.showError('Erreur lors du chargement de la borne');
        console.error(error);
        this.loading = false;
        this.router.navigate(['/dashboard']);
      }
    });
  }

  onLieuChange(event: any): void {
    const lieuId = parseInt(event.target.value);
    const lieu = this.lieux.find(l => l.idLieu === lieuId);
    
    if (lieu) {
      // Mettre à jour la localisation et les coordonnées avec celles du lieu
      this.borneForm.patchValue({
        localisation: this.getFullAddress(lieu),
        latitude: lieu.latitude || '',
        longitude: lieu.longitude || ''
      });
    }
  }

  getFullAddress(lieu: Lieu): string {
    const parts = [];
    if (lieu.numero) parts.push(lieu.numero);
    if (lieu.rue) parts.push(lieu.rue);
    if (lieu.ville) parts.push(lieu.ville);
    return parts.join(', ') || lieu.adresse;
  }

  onSubmit(): void {
    if (this.borneForm.invalid) {
      Object.keys(this.borneForm.controls).forEach(key => {
        this.borneForm.get(key)?.markAsTouched();
      });
      this.toastService.showError('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.toastService.showError('Vous devez être connecté');
      return;
    }

    this.loading = true;
    const borneData = {
      ...this.borneForm.value,
      // Convertir en nombres si nécessaire
      puissance: parseFloat(this.borneForm.value.puissance),
      prix: parseFloat(this.borneForm.value.prix)
    };

    if (this.isEditMode && this.borneId) {
      // Mode édition
      this.borneService.updateBorne(this.borneId, borneData).subscribe({
        next: (response: any) => {
          this.toastService.showSuccess('Borne mise à jour avec succès');
          this.loading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error: any) => {
          this.toastService.showError('Erreur lors de la mise à jour de la borne');
          console.error(error);
          this.loading = false;
        }
      });
    } else {
      // Mode création
      this.borneService.createBorne(borneData).subscribe({
        next: (response: any) => {
          this.toastService.showSuccess('Borne créée avec succès');
          this.loading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error: any) => {
          this.toastService.showError('Erreur lors de la création de la borne');
          console.error(error);
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
