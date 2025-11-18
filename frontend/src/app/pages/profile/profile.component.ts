import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UtilisateurService } from '../../services/utilisateur.service';
import { Utilisateur } from '../../models/utilisateur.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  editMode = false;
  changePasswordMode = false;
  
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private utilisateurService: UtilisateurService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    if (this.currentUser) {
      this.loadUserData();
    }
    
    this.initForms();
  }

  private loadUserData(): void {
    if (this.currentUser) {
      this.utilisateurService.getUtilisateurById(this.currentUser.idUtilisateur)
        .subscribe({
          next: (response) => {
            if (response.result === 'SUCCESS' && response.data) {
              this.currentUser = response.data;
              this.updateFormValues();
            }
          },
          error: (error) => {
            this.showError('Impossible de charger vos informations');
          }
        });
    }
  }

  private initForms(): void {
    this.profileForm = this.fb.group({
      nom: [this.currentUser?.nom || '', Validators.required],
      prenom: [this.currentUser?.prenom || '', Validators.required],
      email: [this.currentUser?.email || '', [Validators.required, Validators.email]],
      pseudo: [{ value: this.currentUser?.pseudo || '', disabled: true }]
    });

    this.passwordForm = this.fb.group({
      ancienMotDePasse: ['', [Validators.required, Validators.minLength(6)]],
      nouveauMotDePasse: ['', [Validators.required, Validators.minLength(6)]],
      confirmMotDePasse: ['', [Validators.required, Validators.minLength(6)]]
    }, { validators: this.passwordMatchValidator });
  }

  private updateFormValues(): void {
    if (this.currentUser) {
      this.profileForm.patchValue({
        nom: this.currentUser.nom,
        prenom: this.currentUser.prenom,
        email: this.currentUser.email,
        pseudo: this.currentUser.pseudo
      });
    }
  }

  private passwordMatchValidator(form: FormGroup) {
    const nouveauMotDePasse = form.get('nouveauMotDePasse')?.value;
    const confirmMotDePasse = form.get('confirmMotDePasse')?.value;
    
    return nouveauMotDePasse === confirmMotDePasse ? null : { passwordMismatch: true };
  }

  toggleEditMode(): void {
    this.editMode = !this.editMode;
    if (!this.editMode) {
      this.updateFormValues(); // Reset form if cancelled
      this.clearMessages();
    }
  }

  toggleChangePasswordMode(): void {
    this.changePasswordMode = !this.changePasswordMode;
    if (!this.changePasswordMode) {
      this.passwordForm.reset();
      this.clearMessages();
    }
  }

  saveProfile(): void {
    if (this.profileForm.valid && this.currentUser) {
      this.loading = true;
      this.clearMessages();

      const updatedUser: Utilisateur = {
        ...this.currentUser,
        ...this.profileForm.value,
        pseudo: this.currentUser.pseudo // Le pseudo ne change pas
      };

      this.utilisateurService.updateUtilisateur(this.currentUser.idUtilisateur, updatedUser)
        .subscribe({
          next: (response) => {
            this.loading = false;
            if (response.result === 'SUCCESS' && response.data) {
              this.currentUser = response.data;
              this.authService.updateCurrentUser(response.data);
              this.showSuccess('Profil mis à jour avec succès');
              this.editMode = false;
            }
          },
          error: (error) => {
            this.loading = false;
            this.showError(error.error?.message || 'Erreur lors de la mise à jour du profil');
          }
        });
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid && this.currentUser) {
      this.loading = true;
      this.clearMessages();

      const { ancienMotDePasse, nouveauMotDePasse } = this.passwordForm.value;
      
      this.utilisateurService.changePassword(this.currentUser.idUtilisateur, {
        ancienMotDePasse,
        nouveauMotDePasse
      }).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.result === 'SUCCESS') {
            this.showSuccess('Mot de passe changé avec succès');
            this.passwordForm.reset();
            this.changePasswordMode = false;
          }
        },
        error: (error) => {
          this.loading = false;
          this.showError(error.error?.message || 'Erreur lors du changement de mot de passe');
        }
      });
    }
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => this.successMessage = '', 5000);
  }

  private showError(message: string): void {
    this.errorMessage = message;
    setTimeout(() => this.errorMessage = '', 5000);
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getRoleBadgeClass(): string {
    switch (this.currentUser?.role) {
      case 'admin': return 'badge-admin';
      case 'user': return 'badge-user';
      default: return 'badge-default';
    }
  }

  getRoleLabel(): string {
    switch (this.currentUser?.role) {
      case 'admin': return 'Administrateur';
      case 'user': return 'Utilisateur';
      default: return 'Utilisateur';
    }
  }
} 