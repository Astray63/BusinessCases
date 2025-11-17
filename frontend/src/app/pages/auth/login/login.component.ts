import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService, AuthResponse } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  returnUrl: string = '/dashboard';
  queryParams: any = {};

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {
    this.loginForm = this.fb.group({
      pseudo: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Récupérer l'URL de retour et les query params
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/dashboard';
      // Conserver tous les autres query params sauf returnUrl
      const { returnUrl, ...rest } = params;
      this.queryParams = rest;
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { pseudo, password } = this.loginForm.value;

    this.authService.login({ pseudo, password }).subscribe({
      next: (response: ApiResponse<AuthResponse>) => {
        if (response.result === 'SUCCESS') {
          this.toastService.showSuccess(response.message || 'Connexion réussie');
          // Rediriger vers l'URL de retour avec les query params
          this.router.navigate([this.returnUrl], { queryParams: this.queryParams });
        } else {
          this.toastService.showError(response.message || 'Invalid credentials');
        }
      },
      error: (err: any) => {
        this.toastService.showError(err.error?.message || 'Invalid credentials');
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
