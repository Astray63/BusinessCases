import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { BorneService } from '../../../services/borne.service';
import { LieuService } from '../../../services/lieu.service';
import { ToastService } from '../../../services/toast.service';
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
  
  // Gestion des photos
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  existingPhotos: string[] = [];

  constructor(
    private authService: AuthService,
    private borneService: BorneService,
    private lieuService: LieuService,
    private router: Router,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.borneForm = this.fb.group({
      nom: ['', Validators.required],
      localisation: ['', Validators.required],
      type: ['Type 2', Validators.required],
      puissance: ['', [Validators.required, Validators.min(1)]],
      prix: ['', [Validators.required, Validators.min(0)]],
      etat: ['DISPONIBLE', Validators.required],
      lieu: [null, Validators.required],
      ville: ['', Validators.required],
      latitude: ['', Validators.required],
      longitude: ['', Validators.required],
      instruction: [''],
      surPied: [false]
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
    this.lieuService.getByUtilisateur(this.currentUser.idUtilisateur).subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.mesLieux = response.data;
        } else if (Array.isArray(response)) {
          this.mesLieux = response;
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
    this.selectedFiles = [];
    this.previewUrls = [];
    this.existingPhotos = [];
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
    this.selectedFiles = [];
    this.previewUrls = [];
    this.existingPhotos = borne.medias || [];
    this.showModal = true;
  }

  fermerModal(): void {
    this.showModal = false;
    this.borneForm.reset();
    this.selectedBorne = null;
    this.selectedFiles = [];
    this.previewUrls = [];
    this.existingPhotos = [];
  }

  onFileSelected(event: any): void {
    const files: FileList = event.target.files;
    if (files && files.length > 0) {
      // Limiter à 5 photos maximum
      const maxPhotos = 5;
      const remainingSlots = maxPhotos - (this.existingPhotos.length + this.selectedFiles.length);
      
      if (remainingSlots <= 0) {
        this.toastService.showWarning(`Vous pouvez ajouter maximum ${maxPhotos} photos au total.`);
        return;
      }

      const filesToAdd = Math.min(files.length, remainingSlots);
      
      for (let i = 0; i < filesToAdd; i++) {
        const file = files[i];
        
        // Vérifier le type de fichier
        if (!file.type.startsWith('image/')) {
          this.toastService.showError('Seules les images sont autorisées');
          continue;
        }
        
        // Vérifier la taille (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
          this.toastService.showError('La taille maximale par image est de 5MB');
          continue;
        }
        
        this.selectedFiles.push(file);
        
        // Créer une preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.previewUrls.push(e.target.result);
        };
        reader.readAsDataURL(file);
      }
      
      if (filesToAdd < files.length) {
        this.toastService.showWarning(`Seulement ${filesToAdd} photo(s) ont été ajoutée(s) (limite de ${maxPhotos} photos atteinte).`);
      }
    }
  }

  removeNewPhoto(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
  }

  removeExistingPhoto(index: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette photo ?')) {
      this.existingPhotos.splice(index, 1);
    }
  }

  // Simulation de l'upload de photos (à remplacer par un vrai upload vers le backend)
  private async uploadPhotos(): Promise<string[]> {
    const uploadedUrls: string[] = [];
    
    // Pour l'instant, on simule l'upload en convertissant les fichiers en base64
    // Dans une vraie implémentation, il faudrait envoyer les fichiers au backend
    for (const file of this.selectedFiles) {
      const base64 = await this.fileToBase64(file);
      uploadedUrls.push(base64 as string);
    }
    
    return uploadedUrls;
  }

  private fileToBase64(file: File): Promise<string | ArrayBuffer | null> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = error => reject(error);
      reader.readAsDataURL(file);
    });
  }

  async soumettreBorne(): Promise<void> {
    if (this.borneForm.invalid || !this.currentUser) return;
    
    this.isLoading = true;
    const formData = this.borneForm.value;
    
    // Convertir l'ID du lieu en nombre
    const lieuId = typeof formData.lieu === 'string' ? parseInt(formData.lieu, 10) : formData.lieu;
    
    console.log('Form data lieu:', formData.lieu, 'Type:', typeof formData.lieu);
    console.log('Lieu ID converti:', lieuId);
    console.log('Mes lieux disponibles:', this.mesLieux.map(l => ({ id: l.idLieu, nom: l.nom })));
    
    // Récupérer le lieu sélectionné
    const lieuSelectionne = this.mesLieux.find(l => l.idLieu === lieuId);
    
    console.log('Lieu sélectionné:', lieuSelectionne);
    
    if (!lieuSelectionne) {
      this.toastService.showError('Veuillez sélectionner un lieu valide');
      this.isLoading = false;
      return;
    }
    
    // Calculer le prix à la minute à partir du tarif horaire
    const prixHoraire = parseFloat(formData.prix);
    const prixMinute = (prixHoraire / 60).toFixed(4);
    
    const borneData: any = {
      numero: `BORNE-${Date.now()}`, // Générer un numéro unique
      nom: formData.nom || `${lieuSelectionne.nom} - ${formData.type}`,
      localisation: formData.localisation,
      address: `${formData.ville}`, // Utiliser la ville du formulaire
      latitude: parseFloat(formData.latitude) || 0,
      longitude: parseFloat(formData.longitude) || 0,
      type: formData.type,
      connectorType: formData.type,
      puissance: parseFloat(formData.puissance),
      prix: prixHoraire,
      hourlyRate: prixHoraire,
      prixALaMinute: parseFloat(prixMinute),
      etat: formData.etat,
      ownerId: this.currentUser.idUtilisateur,
      lieu: { idLieu: lieuId },
      instruction: formData.instruction || '',
      surPied: formData.surPied || false,
      medias: this.existingPhotos // Garder les photos existantes
    };

    if (this.isEditMode && this.selectedBorne && this.selectedBorne.idBorne) {
      // Modification
      borneData.idBorne = this.selectedBorne.idBorne;
      borneData.id = this.selectedBorne.idBorne;
      this.borneService.updateBorne(this.selectedBorne.idBorne, borneData).subscribe({
        next: async (response) => {
          if (response.result === 'SUCCESS') {
            // Upload des nouvelles photos après la mise à jour de la borne
            if (this.selectedFiles.length > 0) {
              try {
                await this.uploadPhotosToServer(this.selectedBorne!.idBorne!);
                this.toastService.showSuccess('Borne et photos modifiées avec succès !');
              } catch (error) {
                console.error('Erreur lors de l\'upload des photos:', error);
                this.toastService.showWarning('Borne modifiée, mais erreur lors de l\'upload des photos');
              }
            } else {
              this.toastService.showSuccess('Borne modifiée avec succès !');
            }
            this.fermerModal();
            this.chargerDonnees();
          }
        },
        error: (error) => {
          console.error('Erreur lors de la modification:', error);
          this.toastService.showError('Erreur lors de la modification de la borne');
          this.isLoading = false;
        }
      });
    } else {
      // Ajout
      this.borneService.createBorne(borneData).subscribe({
        next: async (response) => {
          if (response.result === 'SUCCESS' && response.data) {
            const borneId = response.data.idBorne || response.data.id;
            
            // Upload des photos après la création de la borne
            if (this.selectedFiles.length > 0 && borneId) {
              try {
                await this.uploadPhotosToServer(borneId);
                this.toastService.showSuccess('Borne et photos ajoutées avec succès !');
              } catch (error) {
                console.error('Erreur lors de l\'upload des photos:', error);
                this.toastService.showWarning('Borne créée, mais erreur lors de l\'upload des photos');
              }
            } else {
              this.toastService.showSuccess('Borne ajoutée avec succès !');
            }
            this.fermerModal();
            this.chargerDonnees();
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'ajout:', error);
          this.toastService.showError('Erreur lors de l\'ajout de la borne');
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
          this.toastService.showSuccess('Borne supprimée avec succès !');
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        this.toastService.showError('Erreur lors de la suppression de la borne');
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
          this.toastService.showSuccess(`État changé en ${nouvelEtat}`);
          this.chargerDonnees();
        }
      },
      error: (error) => {
        console.error('Erreur lors du changement d\'état:', error);
        this.toastService.showError('Erreur lors du changement d\'état');
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
    this.router.navigate(['/proprietaire/mes-lieux']);
  }

  utiliserMaPosition(): void {
    if (!navigator.geolocation) {
      this.toastService.showError('La géolocalisation n\'est pas supportée par votre navigateur');
      return;
    }

    this.isLoading = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.borneForm.patchValue({
          latitude: position.coords.latitude.toFixed(6),
          longitude: position.coords.longitude.toFixed(6)
        });
        this.isLoading = false;
        this.toastService.showSuccess('Position récupérée avec succès !');
      },
      (error) => {
        this.isLoading = false;
        let errorMessage = 'Impossible de récupérer votre position';
        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Vous avez refusé l\'accès à la géolocalisation';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Votre position est indisponible';
            break;
          case error.TIMEOUT:
            errorMessage = 'La demande de géolocalisation a expiré';
            break;
        }
        this.toastService.showError(errorMessage);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  }

  // Upload réel vers le backend
  private uploadPhotosToServer(borneId: number): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.selectedFiles.length === 0) {
        resolve();
        return;
      }

      this.borneService.uploadPhotos(borneId, this.selectedFiles).subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            console.log('Photos uploadées avec succès:', response.data);
            resolve();
          } else {
            reject(new Error('Erreur lors de l\'upload des photos'));
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'upload:', error);
          reject(error);
        }
      });
    });
  }
}
