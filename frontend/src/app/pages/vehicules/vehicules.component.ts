import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VehiculeService } from '../../services/vehicule.service';
import { ToastService } from '../../services/toast.service';
import { LoadingService } from '../../services/loading.service';
import { Vehicule } from '../../models/vehicule.model';
import { ApiResponse } from '../../models/api-response.model';

@Component({
  selector: 'app-vehicules',
  templateUrl: './vehicules.component.html',
  styleUrls: ['./vehicules.component.scss']
})
export class VehiculesComponent implements OnInit {
  vehicules: Vehicule[] = [];
  vehiculeForm: FormGroup;
  isEditing = false;
  currentVehiculeId?: number;

  constructor(
    private fb: FormBuilder,
    private vehiculeService: VehiculeService,
    private toastService: ToastService,
    private loadingService: LoadingService
  ) {
    this.vehiculeForm = this.fb.group({
      marque: ['', Validators.required],
      modele: ['', Validators.required],
      annee: ['', [Validators.required, Validators.min(1900)]],
      immatriculation: ['', Validators.required],
      connecteur: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadVehicules();
  }

  loadVehicules(): void {
    this.loadingService.show();
    this.vehiculeService.getVehicules().subscribe({
      next: (response) => {
        if (response.data) {
          this.vehicules = response.data;
        }
        this.loadingService.hide();
      },
      error: () => {
        this.toastService.showError('Erreur lors du chargement des véhicules');
        this.loadingService.hide();
      }
    });
  }

  onSubmit(): void {
    if (this.vehiculeForm.invalid) {
      return;
    }

    this.loadingService.show();
    const vehiculeData = this.vehiculeForm.value;

    if (this.isEditing && this.currentVehiculeId) {
      this.vehiculeService.updateVehicule(this.currentVehiculeId, vehiculeData).subscribe({
        next: (response) => {
          this.toastService.showSuccess(response.message || 'Véhicule mis à jour avec succès');
          this.loadVehicules();
          this.resetForm();
        },
        error: (error) => {
          this.toastService.showError(error.error?.message || 'Erreur lors de la mise à jour');
          this.loadingService.hide();
        }
      });
    } else {
      this.vehiculeService.addVehicule(vehiculeData).subscribe({
        next: (response) => {
          this.toastService.showSuccess(response.message || 'Véhicule ajouté avec succès');
          this.loadVehicules();
          this.resetForm();
        },
        error: (error) => {
          this.toastService.showError(error.error?.message || 'Erreur lors de l\'ajout');
          this.loadingService.hide();
        }
      });
    }
  }

  editVehicule(vehicule: Vehicule): void {
    this.isEditing = true;
    this.currentVehiculeId = vehicule.idVehicule;
    this.vehiculeForm.patchValue({
      marque: vehicule.marque,
      modele: vehicule.modele,
      annee: vehicule.annee,
      immatriculation: vehicule.immatriculation,
      connecteur: vehicule.connecteur
    });
  }

  deleteVehicule(id: number): void {
    this.loadingService.show();
    this.vehiculeService.deleteVehicule(id).subscribe({
      next: (response) => {
        this.toastService.showSuccess(response.message || 'Véhicule supprimé avec succès');
        this.loadVehicules();
      },
      error: (error) => {
        this.toastService.showError(error.error?.message || 'Erreur lors de la suppression');
        this.loadingService.hide();
      }
    });
  }

  resetForm(): void {
    this.vehiculeForm.reset();
    this.isEditing = false;
    this.currentVehiculeId = undefined;
    this.loadingService.hide();
  }
}
