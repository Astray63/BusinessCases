import { Directive, OnDestroy } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Subject } from 'rxjs';
import { FormValidationService } from '../services/form-validation.service';

@Directive()
export abstract class BaseFormComponent implements OnDestroy {
  form!: FormGroup;
  submitted = false;
  protected destroy$ = new Subject<void>();

  constructor(protected formValidationService: FormValidationService) {}

  protected markFormTouched(): void {
    Object.values(this.form.controls).forEach(control => {
      if (control.invalid) {
        control.markAsTouched();
        control.updateValueAndValidity({ onlySelf: true });
      }
    });
  }

  protected resetForm(): void {
    this.form.reset();
    this.submitted = false;
    Object.values(this.form.controls).forEach(control => {
      control.markAsUntouched();
      control.updateValueAndValidity();
    });
  }

  protected validateForm(): boolean {
    this.submitted = true;
    this.markFormTouched();
    return this.form.valid;
  }

  protected patchForm(data: any): void {
    this.form.patchValue(data);
    this.form.markAsPristine();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}