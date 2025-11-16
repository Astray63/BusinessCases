import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      pseudo: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      dateNaissance: ['', Validators.required],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      confirmMotDePasse: ['', Validators.required],
      acceptTerms: [false, Validators.requiredTrue]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('motDePasse')?.value;
    const confirmPassword = formGroup.get('confirmMotDePasse')?.value;        if (password === confirmPassword) return null;
        return { mustMatch: true };
    }

    get f() { return this.registerForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    
    const userData = {
      nom: this.registerForm.value.nom,
      prenom: this.registerForm.value.prenom,
      pseudo: this.registerForm.value.pseudo,
      email: this.registerForm.value.email,
      dateNaissance: this.registerForm.value.dateNaissance,
      role: 'client',
      iban: '',
      adressePhysique: '',
      medias: ''
    };
    
    this.authService.register(userData, this.registerForm.value.motDePasse)
      .subscribe({
        next: (response) => {
          if (response.result === 'SUCCESS') {
            // Rediriger vers la page de vérification d'email
            this.router.navigate(['/auth/verify-email'], { 
              queryParams: { email: this.registerForm.value.email }
            });
          } else {
            this.errorMessage = response.message || 'Une erreur inconnue est survenue.';
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Registration error:', error);
          this.errorMessage = error.error?.message || 'Une erreur est survenue lors de l\'inscription.';
          this.loading = false;
        }
      });
  }
}