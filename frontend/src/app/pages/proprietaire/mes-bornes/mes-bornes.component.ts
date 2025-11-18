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
  
  // Gestion des photos
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  existingPhotos: string[] = [];

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
        alert(`Vous pouvez ajouter maximum ${maxPhotos} photos au total.`);
        return;
      }

      const filesToAdd = Math.min(files.length, remainingSlots);
      
      for (let i = 0; i < filesToAdd; i++) {
        const file = files[i];
        
        // Vérifier le type de fichier
        if (!file.type.startsWith('image/')) {
          alert('Seules les images sont autorisées');
          continue;
        }
        
        // Vérifier la taille (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
          alert('La taille maximale par image est de 5MB');
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
        alert(`Seulement ${filesToAdd} photo(s) ont été ajoutée(s) (limite de ${maxPhotos} photos atteinte).`);
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
    
    // Récupérer le lieu sélectionné pour obtenir ses coordonnées
    const lieuSelectionne = this.mesLieux.find(l => l.idLieu === lieuId);
    
    console.log('Lieu sélectionné:', lieuSelectionne);
    
    if (!lieuSelectionne) {
      alert('Veuillez sélectionner un lieu valide');
      this.isLoading = false;
      return;
    }
    
    // Calculer le prix à la minute à partir du tarif horaire
    const prixHoraire = parseFloat(formData.prix);
    const prixMinute = (prixHoraire / 60).toFixed(4);
    
    const borneData: any = {
      numero: `BORNE-${Date.now()}`, // Générer un numéro unique
      nom: `${lieuSelectionne.nom} - ${formData.type}`,
      localisation: formData.localisation,
      address: lieuSelectionne.adresse || `${lieuSelectionne.numero || ''} ${lieuSelectionne.rue || ''} ${lieuSelectionne.codePostal} ${lieuSelectionne.ville}`.trim(),
      latitude: lieuSelectionne.latitude || 0,
      longitude: lieuSelectionne.longitude || 0,
      type: formData.type,
      connectorType: formData.type,
      puissance: parseInt(formData.puissance),
      prix: prixHoraire,
      hourlyRate: prixHoraire,
      prixALaMinute: parseFloat(prixMinute),
      etat: formData.etat,
      ownerId: this.currentUser.idUtilisateur,
      lieu: { idLieu: lieuId },
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
                alert('Borne et photos modifiées avec succès !');
              } catch (error) {
                console.error('Erreur lors de l\'upload des photos:', error);
                alert('Borne modifiée, mais erreur lors de l\'upload des photos');
              }
            } else {
              alert('Borne modifiée avec succès !');
            }
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
        next: async (response) => {
          if (response.result === 'SUCCESS' && response.data) {
            const borneId = response.data.idBorne || response.data.id;
            
            // Upload des photos après la création de la borne
            if (this.selectedFiles.length > 0 && borneId) {
              try {
                await this.uploadPhotosToServer(borneId);
                alert('Borne et photos ajoutées avec succès !');
              } catch (error) {
                console.error('Erreur lors de l\'upload des photos:', error);
                alert('Borne créée, mais erreur lors de l\'upload des photos');
              }
            } else {
              alert('Borne ajoutée avec succès !');
            }
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
    this.router.navigate(['/proprietaire/mes-lieux']);
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
