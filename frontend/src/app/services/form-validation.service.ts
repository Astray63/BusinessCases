import { Injectable } from '@angular/core';
import { AbstractControl, ValidationErrors } from '@angular/forms';

type ErrorMessageFunction = (params: any) => string;
type ErrorMessages = {
  [key: string]: string | ErrorMessageFunction;
}

@Injectable({
  providedIn: 'root'
})
export class FormValidationService {
  private errorMessages: ErrorMessages = {
    required: 'Ce champ est obligatoire',
    email: 'Veuillez entrer une adresse email valide',
    minlength: (params: any) => `Minimum ${params.requiredLength} caractères requis`,
    maxlength: (params: any) => `Maximum ${params.requiredLength} caractères autorisés`,
    pattern: 'Format invalide',
    passwordMismatch: 'Les mots de passe ne correspondent pas',
    min: (params: any) => `La valeur minimale est ${params.min}`,
    max: (params: any) => `La valeur maximale est ${params.max}`,
    date: 'Date invalide',
    futureDate: 'La date doit être dans le futur',
    endDateAfterStart: 'La date de fin doit être après la date de début'
  };

  getErrorMessage(control: AbstractControl): string {
    if (!control || !control.errors) {
      return '';
    }

    const firstError = Object.entries(control.errors)[0];
    const errorType = firstError[0];
    const errorValue = firstError[1];

    const errorMessage = this.errorMessages[errorType];
    if (typeof errorMessage === 'function') {
      return errorMessage(errorValue);
    }

    return errorMessage || 'Champ invalide';
  }

  // Validateurs personnalisés
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    return null;
  }

  futureDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }

    const date = new Date(control.value);
    const now = new Date();

    if (date <= now) {
      return { futureDate: true };
    }

    return null;
  }

  dateRangeValidator(startControl: AbstractControl, endControl: AbstractControl): void {
    if (!startControl.value || !endControl.value) {
      return;
    }

    const startDate = new Date(startControl.value);
    const endDate = new Date(endControl.value);

    if (endDate <= startDate) {
      endControl.setErrors({ endDateAfterStart: true });
    } else {
      const currentErrors = { ...endControl.errors };
      if (currentErrors) {
        delete currentErrors['endDateAfterStart'];
        endControl.setErrors(Object.keys(currentErrors).length ? currentErrors : null);
      }
    }
  }
}