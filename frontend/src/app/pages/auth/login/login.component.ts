import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {
    this.loginForm = this.fb.group({
      pseudo: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { pseudo, password } = this.loginForm.value;

    this.authService.login({ pseudo, password }).subscribe({
      next: (response: ApiResponse<{token: string, user: any}>) => {
        if (response.result === 'SUCCESS') {
          this.router.navigate(['/']);
        } else {
          this.toastService.showError(response.message || 'Login failed');
        }
      },
      error: (err: any) => {
        this.toastService.showError('An error occurred during login');
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
