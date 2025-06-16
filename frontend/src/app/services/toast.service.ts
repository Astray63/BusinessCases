import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  title: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastSubject.asObservable();

  get toasts(): Toast[] {
    return this.toastSubject.value;
  }

  showSuccess(message: string, title: string = 'Succès'): void {
    this.show({ type: 'success', message, title });
  }

  showError(message: string, title: string = 'Erreur'): void {
    this.show({ type: 'error', message, title });
  }

  showWarning(message: string, title: string = 'Attention'): void {
    this.show({ type: 'warning', message, title });
  }

  showInfo(message: string, title: string = 'Information'): void {
    this.show({ type: 'info', message, title });
  }

  remove(toast: Toast): void {
    const currentToasts = this.toasts.filter(t => t !== toast);
    this.toastSubject.next(currentToasts);
  }

  private show(toast: Toast): void {
    const currentToasts = [...this.toasts, toast];
    this.toastSubject.next(currentToasts);
    setTimeout(() => this.remove(toast), 5000);
  }
}