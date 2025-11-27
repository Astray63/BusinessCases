import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl } from '@angular/forms';
import { FormValidationService } from '../../services/form-validation.service';

@Component({
  selector: 'app-form-error',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (control && control.invalid && (control.dirty || control.touched)) {
      <div class="invalid-feedback d-block">
        {{ errorMessage }}
      </div>
    }
  `,
  styles: [`
    .invalid-feedback {
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }
  `]
})
export class FormErrorComponent {
  @Input() control: AbstractControl | null = null;

  constructor(private formValidationService: FormValidationService) { }

  get errorMessage(): string {
    if (!this.control) return '';
    return this.formValidationService.getErrorMessage(this.control);
  }
}