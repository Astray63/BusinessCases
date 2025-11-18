import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-validate',
  templateUrl: './validate.component.html',
})
export class ValidateComponent implements OnInit {
  validateForm!: FormGroup;
  loading = false;
  submitted = false;
  email: string = '';
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Rediriger vers verify-email qui est le composant principal de validation
    this.route.queryParams.subscribe(params => {
      const email = params['email'] || '';
      // Redirection vers le composant unifié
      this.router.navigate(['/auth/verify-email'], { 
        queryParams: { email: email },
        replaceUrl: true 
      });
    });
  }

  get f() { return this.validateForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    if (this.validateForm.invalid) {
      return;
    }

    this.loading = true;
    
    this.authService.validateAccount(this.email, this.validateForm.value.code)
      .subscribe({
        next: (response: any) => {
          if (response.result === 'SUCCESS') {
            this.toastService.showSuccess('Compte validé avec succès ! Vous pouvez maintenant vous connecter.');
            this.router.navigate(['/auth/login']);
          } else {
            this.errorMessage = response.message || 'Le code de validation est incorrect.';
            this.loading = false;
          }
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Une erreur est survenue lors de la validation.';
          this.loading = false;
        }
      });
  }

  resendCode(): void {
    if (!this.email) {
      this.toastService.showError('Email manquant. Veuillez vous réinscrire.');
      return;
    }

    this.loading = true;
    this.authService.resendValidationCode(this.email)
      .subscribe({
        next: (response: any) => {
          if (response.result === 'SUCCESS') {
            this.toastService.showSuccess('Un nouveau code a été envoyé par email.');
          } else {
            this.toastService.showError(response.message || 'Erreur lors de l\'envoi du code.');
          }
          this.loading = false;
        },
        error: (error: any) => {
          this.toastService.showError('Erreur lors de l\'envoi du code.');
          this.loading = false;
        }
      });
  }
}
