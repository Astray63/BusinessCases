import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LieuService } from '../../../services/lieu.service';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { Lieu } from '../../../models/lieu.model';

@Component({
  selector: 'app-lieu-form',
  templateUrl: './lieu-form.component.html',
})
export class LieuFormComponent implements OnInit {
  lieuForm: FormGroup;
  isEditMode = false;
  lieuId: number | null = null;
  loading = false;
  pageTitle = 'Ajouter un lieu de recharge';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private lieuService: LieuService,
    private authService: AuthService,
    private toastService: ToastService
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
    // Vérifier si c'est un mode édition
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.lieuId = +params['id'];
        this.pageTitle = 'Modifier le lieu de recharge';
        this.loadLieu(this.lieuId);
      }
    });
  }

  loadLieu(id: number): void {
    this.loading = true;
    this.lieuService.getById(id).subscribe({
      next: (lieu: Lieu) => {
        this.lieuForm.patchValue(lieu);
        this.loading = false;
      },
      error: (error: any) => {
        this.toastService.showError('Erreur lors du chargement du lieu');
        this.loading = false;
        this.router.navigate(['/lieux']);
      }
    });
  }

  onSubmit(): void {
    if (this.lieuForm.invalid) {
      Object.keys(this.lieuForm.controls).forEach(key => {
        this.lieuForm.get(key)?.markAsTouched();
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
    const lieuData: Lieu = this.lieuForm.value;

    if (this.isEditMode && this.lieuId) {
      // Mode édition
      this.lieuService.update(this.lieuId, lieuData).subscribe({
        next: (updatedLieu: Lieu) => {
          this.toastService.showSuccess('Lieu mis à jour avec succès');
          this.loading = false;
          this.router.navigate(['/lieux']);
        },
        error: (error: any) => {
          this.toastService.showError('Erreur lors de la mise à jour du lieu');
          this.loading = false;
        }
      });
    } else {
      // Mode création
      this.lieuService.create(lieuData, currentUser.idUtilisateur).subscribe({
        next: (newLieu: Lieu) => {
          this.toastService.showSuccess('Lieu créé avec succès');
          this.loading = false;
          this.router.navigate(['/lieux']);
        },
        error: (error: any) => {
          this.toastService.showError('Erreur lors de la création du lieu');
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/lieux']);
  }

  // Méthode pour géocoder l'adresse (optionnel)
  geocodeAddress(): void {
    const adresse = this.lieuForm.get('adresse')?.value;
    const ville = this.lieuForm.get('ville')?.value;
    const codePostal = this.lieuForm.get('codePostal')?.value;
    
    if (!adresse || !ville) {
      this.toastService.showError('Veuillez renseigner l\'adresse et la ville');
      return;
    }

    const fullAddress = `${adresse}, ${codePostal} ${ville}`;
    
    // Utiliser l'API Nominatim d'OpenStreetMap pour le géocodage
    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(fullAddress)}`)
      .then(response => response.json())
      .then(data => {
        if (data && data.length > 0) {
          this.lieuForm.patchValue({
            latitude: parseFloat(data[0].lat),
            longitude: parseFloat(data[0].lon)
          });
          this.toastService.showSuccess('Coordonnées GPS trouvées');
        } else {
          this.toastService.showError('Impossible de trouver les coordonnées GPS');
        }
      })
      .catch(error => {
        this.toastService.showError('Erreur lors du géocodage');
      });
  }
}
