import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EmailVerificationService } from '../../../services/email-verification.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
})
export class VerifyEmailComponent implements OnInit {
  verificationForm: FormGroup;
  email: string = '';
  isLoading = false;
  isResending = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private emailVerificationService: EmailVerificationService,
    private toastService: ToastService
  ) {
    this.verificationForm = this.fb.group({
      code: ['', [
        Validators.required,
        Validators.pattern(/^[0-9]{6}$/)
      ]]
    });
  }

  ngOnInit(): void {
    // Récupérer l'email depuis les paramètres de route
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
      if (!this.email) {
        this.toastService.showError('Email non fourni');
        this.router.navigate(['/auth/login']);
      }
    });
  }

  onSubmit(): void {
    if (this.verificationForm.valid && this.email) {
      this.isLoading = true;
      const request = {
        email: this.email,
        code: this.verificationForm.value.code
      };

      this.emailVerificationService.verifyEmail(request).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.toastService.showSuccess(response.message || 'Email vérifié avec succès !');
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Code de vérification incorrect';
          this.toastService.showError(errorMessage);
        }
      });
    }
  }

  resendCode(): void {
    if (!this.email) {
      this.toastService.showError('Email non fourni');
      return;
    }

    this.isResending = true;
    const request = { email: this.email };

    this.emailVerificationService.resendVerificationCode(request).subscribe({
      next: (response) => {
        this.isResending = false;
        this.toastService.showSuccess(response.message || 'Code renvoyé avec succès');
        this.verificationForm.reset();
      },
      error: (error) => {
        this.isResending = false;
        const errorMessage = error.error?.message || 'Erreur lors du renvoi du code';
        this.toastService.showError(errorMessage);
      }
    });
  }

  backToLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  // Permet seulement les chiffres
  onCodeInput(event: any): void {
    const input = event.target;
    input.value = input.value.replace(/[^0-9]/g, '');
    if (input.value.length > 6) {
      input.value = input.value.slice(0, 6);
    }
    this.verificationForm.patchValue({ code: input.value });
  }
}
