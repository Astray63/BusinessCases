import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReservationService } from '../../services/reservation.service';
import { BorneService } from '../../services/borne.service';
import { Borne } from '../../models/borne.model';
import { Reservation } from '../../models/reservation.model';
import { ToastService } from '../../services/toast.service';
import { AuthService } from '../../services/auth.service';
import { ApiResponse } from '../../models/api-response.model';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.scss']
})
export class ReservationComponent implements OnInit {
  reservationForm: FormGroup;
  bornes: Borne[] = [];
  reservations: Reservation[] = [];
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    private borneService: BorneService,
    private toastService: ToastService,
    private authService: AuthService
  ) {
    this.reservationForm = this.fb.group({
      borneId: ['', Validators.required],
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      vehicleType: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadBornes();
    this.loadReservations();
  }

  loadBornes(): void {
    this.borneService.getAllBornes().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.bornes = response.data || [];
      },
      error: (err: any) => this.toastService.showError('Erreur lors du chargement des bornes')
    });
  }

  loadReservations(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.reservationService.getReservationsByCurrentUser(currentUser.idUtilisateur).subscribe({
        next: (response: ApiResponse<Reservation[]>) => this.reservations = response.data || [],
        error: (err: any) => this.toastService.showError('Erreur lors du chargement des réservations')
      });
    }
  }

  onSubmit(): void {
    if (this.reservationForm.invalid) {
      this.toastService.showError('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.isLoading = true;
    const formValues = this.reservationForm.value;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) { this.toastService.showError('Utilisateur non authentifié'); return; }

    const dateDebut = new Date(formValues.date + 'T' + formValues.startTime);
    const dateFin = new Date(formValues.date + 'T' + formValues.endTime);

    const reservationPayload: any = {
      utilisateurId: currentUser.idUtilisateur,
      chargingStationId: formValues.borneId,
      dateDebut: dateDebut.toISOString(),
      dateFin: dateFin.toISOString()
    };
    
    this.reservationService.createReservation(reservationPayload).subscribe({
      next: (response: ApiResponse<Reservation>) => {
        this.toastService.showSuccess(response.message || 'Réservation créée avec succès');
        this.reservationForm.reset();
        this.loadReservations();
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors de la création de la réservation';
        this.toastService.showError(errorMsg);
      },
      complete: () => this.isLoading = false
    });
  }

  getBorneName(borneId: number): string {
    const borne = this.bornes.find(b => b.idBorne === borneId);
    return borne ? `Borne ${borne.idBorne} (${borne.localisation})` : 'Borne inconnue';
  }

  cancelReservation(reservationId: number): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) { return; }
    this.reservationService.cancelReservation(reservationId, currentUser.idUtilisateur).subscribe({
      next: (response: ApiResponse<any>) => {
        this.toastService.showSuccess(response.message || 'Réservation annulée avec succès');
        this.loadReservations();
      },
      error: (err: any) => {
        const errorMsg = err.error?.message || 'Erreur lors de l\'annulation';
        this.toastService.showError(errorMsg);
      }
    });
  }
}
