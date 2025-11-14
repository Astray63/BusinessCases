import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Utilisateur } from '../models/utilisateur.model';
import { ApiResponse } from '../models/api-response.model';

export interface PasswordChangeRequest {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
}

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = `${environment.apiUrl}/utilisateurs`;

  constructor(private http: HttpClient) {}

  getUtilisateurById(id: number): Observable<ApiResponse<Utilisateur>> {
    return this.http.get<ApiResponse<Utilisateur>>(`${this.apiUrl}/${id}`);
  }

  updateUtilisateur(id: number, utilisateur: Utilisateur): Observable<ApiResponse<Utilisateur>> {
    return this.http.put<ApiResponse<Utilisateur>>(`${this.apiUrl}/${id}`, utilisateur);
  }

  changePassword(userId: number, request: PasswordChangeRequest): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${userId}/change-password`, request);
  }

  getAllUtilisateurs(): Observable<ApiResponse<Utilisateur[]>> {
    return this.http.get<ApiResponse<Utilisateur[]>>(this.apiUrl);
  }

  deleteUtilisateur(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
