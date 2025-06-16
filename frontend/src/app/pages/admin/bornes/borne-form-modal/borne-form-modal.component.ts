import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Borne } from '../../../../models/borne.model';

@Component({
  selector: 'app-borne-form-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="modal-header">
      <h4 class="modal-title">{{borne ? 'Modifier' : 'Ajouter'}} une borne</h4>
      <button type="button" class="btn-close" aria-label="Close" (click)="activeModal.dismiss()"></button>
    </div>
    <div class="modal-body">
      <form [formGroup]="borneForm">
        <div class="mb-3">
          <label for="localisation" class="form-label">Localisation</label>
          <input type="text" class="form-control" id="localisation" formControlName="localisation">
          <div *ngIf="borneForm.get('localisation')?.errors?.['required'] && borneForm.get('localisation')?.touched" 
               class="text-danger">
            La localisation est requise
          </div>
        </div>

        <div class="row mb-3">
          <div class="col-md-6">
            <label for="latitude" class="form-label">Latitude</label>
            <input type="number" class="form-control" id="latitude" formControlName="latitude">
            <div *ngIf="borneForm.get('latitude')?.errors?.['required'] && borneForm.get('latitude')?.touched" 
                 class="text-danger">
              La latitude est requise
            </div>
          </div>
          <div class="col-md-6">
            <label for="longitude" class="form-label">Longitude</label>
            <input type="number" class="form-control" id="longitude" formControlName="longitude">
            <div *ngIf="borneForm.get('longitude')?.errors?.['required'] && borneForm.get('longitude')?.touched" 
                 class="text-danger">
              La longitude est requise
            </div>
          </div>
        </div>

        <div class="mb-3">
          <label for="type" class="form-label">Type de borne</label>
          <select class="form-select" id="type" formControlName="type">
            <option value="NORMALE">Normale</option>
            <option value="RAPIDE">Rapide</option>
          </select>
        </div>

        <div class="mb-3">
          <label for="puissance" class="form-label">Puissance (kW)</label>
          <input type="number" class="form-control" id="puissance" formControlName="puissance">
          <div *ngIf="borneForm.get('puissance')?.errors?.['required'] && borneForm.get('puissance')?.touched" 
               class="text-danger">
            La puissance est requise
          </div>
        </div>

        <div class="mb-3">
          <label for="prix" class="form-label">Prix (€/kWh)</label>
          <input type="number" class="form-control" id="prix" formControlName="prix" step="0.01">
          <div *ngIf="borneForm.get('prix')?.errors?.['required'] && borneForm.get('prix')?.touched" 
               class="text-danger">
            Le prix est requis
          </div>
        </div>

        <div class="mb-3">
          <label for="etat" class="form-label">État</label>
          <select class="form-select" id="etat" formControlName="etat">
            <option value="DISPONIBLE">Disponible</option>
            <option value="HORS_SERVICE">Hors service</option>
          </select>
        </div>
      </form>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-secondary" (click)="activeModal.dismiss()">Annuler</button>
      <button type="button" class="btn btn-primary" (click)="onSubmit()" [disabled]="!borneForm.valid">
        {{borne ? 'Modifier' : 'Ajouter'}}
      </button>
    </div>
  `
})
export class BorneFormModalComponent implements OnInit {
  @Input() borne?: Borne;
  borneForm!: FormGroup;

  constructor(
    public activeModal: NgbActiveModal,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.borneForm = this.formBuilder.group({
      localisation: [this.borne?.localisation || '', Validators.required],
      type: [this.borne?.type || 'NORMALE', Validators.required],
      puissance: [this.borne?.puissance || '', Validators.required],
      etat: [this.borne?.etat || 'DISPONIBLE', Validators.required],
      prix: [this.borne?.prix || '', Validators.required],
      latitude: [this.borne?.latitude || '', Validators.required],
      longitude: [this.borne?.longitude || '', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.borneForm.valid) {
      const borneData = {
        ...this.borne,
        ...this.borneForm.value
      };
      this.activeModal.close(borneData);
    }
  }
}