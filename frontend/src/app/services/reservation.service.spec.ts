import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReservationService } from './reservation.service';
import { Reservation, ReservationStatus } from '../models/reservation.model';
import { Utilisateur } from '../models/utilisateur.model';
import { Borne, BorneStatus, BorneType } from '../models/borne.model';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

import { ReservationMapperService } from './reservation-mapper.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;

  const mockUtilisateur: Utilisateur = {
    idUtilisateur: 1,
    pseudo: 'testuser',
    nom: 'Test',
    prenom: 'User',
    email: 'test@example.com',
    role: 'user',
    actif: true,
    dateCreation: new Date(),
    dateModification: new Date()
  };

  const mockBorne: Borne = {
    idBorne: 1,
    localisation: 'Test Location',
    type: 'NORMALE' as BorneType,
    puissance: 22,
    etat: 'DISPONIBLE' as BorneStatus,
    prix: 2.5,
    latitude: 45.0,
    longitude: 5.0,
    dateCreation: new Date(),
    dateModification: new Date()
  };

  const mockReservations: Reservation[] = [
    {
      idReservation: 1,
      dateDebut: new Date(),
      dateFin: new Date(),
      utilisateur: mockUtilisateur,
      borne: mockBorne,
      statut: 'EN_ATTENTE',
      dateCreation: new Date(),
      dateModification: new Date(),
      montantTotal: 25.0
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ReservationService,
        {
          provide: ReservationMapperService,
          useValue: {
            mapArrayToFrontend: (data: any) => data,
            mapToFrontend: (data: any) => data
          }
        }
      ]
    });
    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllReservations', () => {
    it('should return an array of reservations', () => {
      const mockResponse: ApiResponse<Reservation[]> = {
        result: 'SUCCESS',
        message: 'Reservations retrieved successfully',
        data: mockReservations
      };

      service.getAllReservations().subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(response.data?.[0].idReservation).toBe(1);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getReservationById', () => {
    it('should return a reservation by id', () => {
      const mockResponse: ApiResponse<Reservation> = {
        result: 'SUCCESS',
        message: 'Reservation retrieved successfully',
        data: mockReservations[0]
      };

      service.getReservationById(1).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(response.data?.idReservation).toBe(1);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('createReservation', () => {
    it('should create a new reservation', () => {
      const newReservation: Partial<Reservation> = {
        borne: mockBorne,
        dateDebut: new Date(),
        dateFin: new Date()
      };

      const mockResponse: ApiResponse<Reservation> = {
        result: 'SUCCESS',
        message: 'Reservation created successfully',
        data: { ...mockReservations[0], ...newReservation }
      };

      service.createReservation(newReservation).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(response.data?.idReservation).toBe(1);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newReservation);
      req.flush(mockResponse);
    });
  });

  describe('updateReservation', () => {
    it('should update an existing reservation', () => {
      const updateData: Partial<Reservation> = {
        statut: 'CONFIRMEE' as ReservationStatus
      };

      const mockResponse: ApiResponse<Reservation> = {
        result: 'SUCCESS',
        message: 'Reservation updated successfully',
        data: { ...mockReservations[0], ...updateData }
      };

      service.updateReservation(1, updateData).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(response.data?.statut).toBe('CONFIRMEE');
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateData);
      req.flush(mockResponse);
    });
  });

  describe('cancelReservation', () => {
    it('should cancel a reservation', () => {
      const mockResponse: ApiResponse<void> = {
        result: 'SUCCESS',
        message: 'Reservation cancelled successfully',
        data: undefined
      };

      service.cancelReservation(1).subscribe((response: ApiResponse<void>) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations/1/cancel`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  describe('getReservationsByUser', () => {
    it('should return reservations for a user', () => {
      const mockResponse: ApiResponse<Reservation[]> = {
        result: 'SUCCESS',
        message: 'Reservations retrieved successfully',
        data: mockReservations
      };

      service.getReservationsByUser(1).subscribe((response: ApiResponse<Reservation[]>) => {
        expect(response).toEqual(mockResponse);
        expect(response.data?.[0].utilisateur.idUtilisateur).toBe(1);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/reservations/utilisateur/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });
});
