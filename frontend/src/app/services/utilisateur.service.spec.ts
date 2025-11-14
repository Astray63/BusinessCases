import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UtilisateurService, PasswordChangeRequest } from './utilisateur.service';
import { environment } from '../../environments/environment';
import { Utilisateur } from '../models/utilisateur.model';
import { ApiResponse } from '../models/api-response.model';

describe('UtilisateurService', () => {
  let service: UtilisateurService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UtilisateurService]
    });
    service = TestBed.inject(UtilisateurService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUtilisateurById', () => {
    it('should retrieve a user by id', () => {
      const mockUser: Utilisateur = {
        idUtilisateur: 1,
        email: 'test@example.com',
        pseudo: 'testuser',
        nom: 'Test',
        prenom: 'User',
        role: 'client',
        actif: true
      };

      service.getUtilisateurById(1).subscribe((response: ApiResponse<Utilisateur>) => {
        expect(response.data).toEqual(mockUser);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/utilisateurs/1`);
      expect(req.request.method).toBe('GET');
      req.flush({ result: 'SUCCESS', data: mockUser });
    });
  });

  describe('updateUtilisateur', () => {
    it('should update a user', () => {
      const mockUser: Utilisateur = {
        idUtilisateur: 1,
        email: 'updated@example.com',
        pseudo: 'testuser',
        nom: 'Updated',
        prenom: 'User',
        role: 'client',
        actif: true
      };

      service.updateUtilisateur(1, mockUser).subscribe((response: ApiResponse<Utilisateur>) => {
        expect(response.data).toEqual(mockUser);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/utilisateurs/1`);
      expect(req.request.method).toBe('PUT');
      req.flush({ result: 'SUCCESS', data: mockUser });
    });
  });

  describe('changePassword', () => {
    it('should change user password', () => {
      const request: PasswordChangeRequest = {
        ancienMotDePasse: 'oldPassword123',
        nouveauMotDePasse: 'newPassword123'
      };

      service.changePassword(1, request).subscribe((response: ApiResponse<void>) => {
        expect(response.result).toBe('SUCCESS');
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/utilisateurs/1/change-password`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush({ result: 'SUCCESS', message: 'Mot de passe changé avec succès' });
    });
  });
});
