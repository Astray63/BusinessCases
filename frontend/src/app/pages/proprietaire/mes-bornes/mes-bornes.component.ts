import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { BorneService } from '../../../services/borne.service';
import { LieuService } from '../../../services/lieu.service';
import { Utilisateur } from '../../../models/utilisateur.model';
import { Borne } from '../../../models/borne.model';
import { Lieu } from '../../../models/lieu.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-mes-bornes',
  templateUrl: './mes-bornes.component.html',
})
export class MesBornesComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  mesBornes: Borne[] = [];
  mesLieux: Lieu[] = [];
  isLoading = false;
  showModal = false;
  isEditMode = false;
  
  borneForm: FormGroup;
  selectedBorne: Borne | null = null;

  constructor(
    private authService: AuthService,
    private borneService: BorneService,
    private lieuService: LieuService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.borneForm = this.fb.group({
      localisation: ['', Validators.required],
      type: ['Type 2', Validators.required],
      puissance: ['', [Validators.required, Validators.min(1)]],
      prix: ['', [Validators.required, Validators.min(0)]],
      etat: ['DISPONIBLE', Validators.required],
      lieu: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // Plus besoin de vérifier le rôle - le ProprietaireGuard s'en occupe
      if (!user) {
        this.router.navigate(['/auth/login']);
        return;
      }
      
      this.chargerDonnees();
    });
  }

  chargerDonnees(): void {
    if (!this.currentUser || !this.currentUser.idUtilisateur) return;
    
    this.isLoading = true;
    
    // Charger uniquement les bornes du propriétaire connecté
    this.borneService.getBornesByProprietaire(this.currentUser.idUtilisateur).subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.mesBornes = response.data;
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des bornes:', error);
        this.isLoading = false;
      }
    });

    // Charger les lieux du propriétaire
    this.lieuService.getLieux().subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.mesLieux = response.data;
        }
      },
      error: (error: any) => console.error('Erreur lors du chargement des lieux:', error)
    });
  }

  ouvrirModalAjout(): void {
    this.isEditMode = false;
    this.selectedBorne = null;
    this.borneForm.reset({
      type: 'Type 2',
      etat: 'DISPONIBLE'
    });
    this.showModal = true;
  }

  ouvrirModalModification(borne: Borne): void {
    this.isEditMode = true;
    this.selectedBorne = borne;
    this.borneForm.patchValue({
      localisation: borne.localisation,
      type: borne.type,
      puissance: borne.puissance,
      prix: borne.prix,
      etat: borne.etat,
      lieu: (borne as any).lieu?.idLieu || null
    });
    this.showModal = true;
  }

  fermerModal(): void {
    this.showModal = false;
    this.borneForm.reset();
    this.selectedBorne = null;
  }

  soumettreBorne(): void {
    if (this.borneForm.invalid || !this.currentUser) return;
    
    this.isLoading = true;
    const formData = this.borneForm.value;
    
    const borneData: any = {
      localisation: formData.localisation,
      type: formData.type,
      puissance: formData.puissance,
      prix: formData.prix,
      etat: formData.etat,
      lieu: { idLieu: formData.lieu },
      ownerId: this.currentUser.idUtilisateur
    };

    if (this.isEditMode && this.selectedBorne && this.selectedBorne.idBorne) {
      // Modification
      borneData.idBorne = this.selectedBorne.idBorne;
      this.borneService.updateBorne(this.selectedBorne.idBorne, borneData).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            alert('Borne modifiée avec succès !');
            this.fermerModal();
            this.chargerDonnees();
          }
        },
        error: (error) => {
          console.error('Erreur lors de la modification:', error);
          alert('Erreur lors de la modification de la borne');
          this.isLoading = false;
        }
      });
    } else {
      // Ajout
      this.borneService.createBorne(borneData).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            alert('Borne ajoutée avec succès !');
            this.fermerModal();
            this.chargerDonnees();
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'ajout:', error);
          alert('Erreur lors de l\'ajout de la borne');
          this.isLoading = false;
        }
      });
    }
  }

  supprimerBorne(idBorne: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette borne ?')) {
      return;
    }
    
    this.isLoading = true;
    this.borneService.deleteBorne(idBorne).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          alert('Borne supprimée avec succès !');
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression de la borne');
        this.isLoading = false;
      }
    });
  }

  changerEtat(idBorne: number, nouvelEtat: string): void {
    const borne = this.mesBornes.find(b => b.idBorne === idBorne);
    if (!borne) return;
    
    this.isLoading = true;
    const borneData: any = { ...borne, etat: nouvelEtat };
    
    this.borneService.updateBorne(idBorne, borneData).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          alert(`État changé en ${nouvelEtat}`);
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors du changement d\'état:', error);
        alert('Erreur lors du changement d\'état');
        this.isLoading = false;
      }
    });
  }

  getEtatClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'DISPONIBLE': 'badge-success',
      'OCCUPE': 'badge-warning',
      'HORS_SERVICE': 'badge-danger',
      'MAINTENANCE': 'badge-secondary'
    };
    return classes[etat] || 'badge-secondary';
  }

  getEtatLabel(etat: string): string {
    const labels: { [key: string]: string } = {
      'DISPONIBLE': 'Disponible',
      'OCCUPE': 'Occupée',
      'HORS_SERVICE': 'Hors service',
      'MAINTENANCE': 'En maintenance'
    };
    return labels[etat] || etat;
  }

  ajouterLieu(): void {
    this.router.navigate(['/lieux']);
  }
}
