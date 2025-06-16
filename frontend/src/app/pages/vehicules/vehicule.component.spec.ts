import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { VehiculesComponent } from './vehicules.component';
import { ApiResponse } from '../../models/api-response.model';
import { VehiculeService } from '../../services/vehicule.service';
import { ToastService } from '../../services/toast.service';
import { LoadingService } from '../../services/loading.service';
import { of, throwError } from 'rxjs';

describe('VehiculesComponent', () => {
  let component: VehiculesComponent;
  let fixture: ComponentFixture<VehiculesComponent>;
  
  let vehiculeService: jasmine.SpyObj<VehiculeService>;
  let toastService: jasmine.SpyObj<ToastService>;
  let loadingService: jasmine.SpyObj<LoadingService>;

  const mockVehicules = [
    {
      idVehicule: 1,
      marque: 'Tesla',
      modele: 'Model 3',
      annee: 2022,
      immatriculation: 'AB-123-CD',
      connecteur: 'Type 2',
      idUtilisateur: 1
    },
    {
      idVehicule: 2,
      marque: 'Renault',
      modele: 'Zoe',
      annee: 2021,
      immatriculation: 'EF-456-GH',
      connecteur: 'Type 2',
      idUtilisateur: 1
    }
  ];

  beforeEach(async () => {
    const vehiculeServiceSpy = jasmine.createSpyObj('VehiculeService', 
      ['getVehicules', 'addVehicule', 'updateVehicule', 'deleteVehicule']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']);
    const loadingServiceSpy = jasmine.createSpyObj('LoadingService', 
      ['show', 'hide']);

    await TestBed.configureTestingModule({
      declarations: [ VehiculesComponent ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        NgbModule
      ],
      providers: [
        { provide: VehiculeService, useValue: vehiculeServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy }
      ]
    })
    .compileComponents();

    vehiculeService = TestBed.inject(VehiculeService) as jasmine.SpyObj<VehiculeService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
    loadingService = TestBed.inject(LoadingService) as jasmine.SpyObj<LoadingService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VehiculesComponent);
    component = fixture.componentInstance;

    vehiculeService.getVehicules.and.returnValue(of({
      result: 'SUCCESS',
      message: 'Véhicules récupérés avec succès',
      data: mockVehicules
    }));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load vehicles on init', () => {
    expect(vehiculeService.getVehicules).toHaveBeenCalled();
    expect(component.vehicules).toEqual(mockVehicules);
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle add vehicle', () => {
    const newVehicule = {
      marque: 'BMW',
      modele: 'i3',
      annee: 2023,
      immatriculation: 'IJ-789-KL',
      connecteur: 'Type 2',
      idUtilisateur: 1
    };

    vehiculeService.addVehicule.and.returnValue(of({
      result: 'SUCCESS',
      message: 'Véhicule ajouté avec succès',
      data: { 
        idVehicule: 3,
        marque: 'BMW',
        modele: 'i3',
        annee: 2023,
        immatriculation: 'IJ-789-KL',
        connecteur: 'Type 2',
        idUtilisateur: 1
      }
    }));

    component.vehiculeForm.patchValue(newVehicule);
    component.onSubmit();

    expect(vehiculeService.addVehicule).toHaveBeenCalledWith(jasmine.objectContaining({
      marque: 'BMW',
      modele: 'i3',
      annee: 2023,
      immatriculation: 'IJ-789-KL',
      connecteur: 'Type 2'
    }));
    expect(toastService.showSuccess).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle add vehicle error', () => {
    const errorResponse = {
      result: 'ERROR',
      message: 'Erreur lors de l\'ajout du véhicule',
      data: undefined
    } as ApiResponse<any>;

    vehiculeService.addVehicule.and.returnValue(throwError(() => errorResponse));

    component.vehiculeForm.patchValue({
      marque: 'BMW',
      modele: 'i3',
      annee: 2023,
      immatriculation: 'IJ-789-KL',
      connecteur: 'Type 2',
      idUtilisateur: 1
    });
    component.onSubmit();

    expect(toastService.showError).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle update vehicle', () => {
    const updatedVehicule = { ...mockVehicules[0], modele: 'Model 3 Performance' };

    vehiculeService.updateVehicule.and.returnValue(of({
      result: 'SUCCESS',
      message: 'Véhicule mis à jour avec succès',
      data: updatedVehicule
    }));

    component.editVehicule(mockVehicules[0]);
    component.vehiculeForm.patchValue({ modele: 'Model 3 Performance' });
    component.onSubmit();

    expect(vehiculeService.updateVehicule).toHaveBeenCalledWith(
      mockVehicules[0].idVehicule,
      jasmine.objectContaining({ modele: 'Model 3 Performance' })
    );
    expect(toastService.showSuccess).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle update vehicle error', () => {
    const errorResponse = {
      result: 'ERROR',
      message: 'Erreur lors de la mise à jour',
      data: undefined
    } as ApiResponse<any>;

    vehiculeService.updateVehicule.and.returnValue(throwError(() => errorResponse));

    component.editVehicule(mockVehicules[0]);
    component.vehiculeForm.patchValue({ modele: 'Model 3 Performance' });
    component.onSubmit();

    expect(toastService.showError).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle delete vehicle', () => {
    vehiculeService.deleteVehicule.and.returnValue(of({
      result: 'SUCCESS',
      message: 'Véhicule supprimé avec succès',
      data: undefined
    }));

    component.deleteVehicule(mockVehicules[0].idVehicule);

    expect(vehiculeService.deleteVehicule).toHaveBeenCalledWith(mockVehicules[0].idVehicule);
    expect(toastService.showSuccess).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle delete vehicle error', () => {
    const errorResponse = {
      result: 'ERROR',
      message: 'Erreur lors de la suppression',
      data: undefined
    } as ApiResponse<void>;

    vehiculeService.deleteVehicule.and.returnValue(throwError(() => errorResponse));

    component.deleteVehicule(mockVehicules[0].idVehicule);

    expect(toastService.showError).toHaveBeenCalledWith(jasmine.any(String));
    expect(loadingService.show).toHaveBeenCalled();
    expect(loadingService.hide).toHaveBeenCalled();
  });
});
