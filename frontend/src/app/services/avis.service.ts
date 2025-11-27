import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Avis, CreateAvisRequest } from '../models/avis.model';

@Injectable({
  providedIn: 'root'
})
export class AvisService {
  private apiUrl = `${environment.apiUrl}/avis`;

  constructor(private http: HttpClient) { }

  /**
   * Récupère tous les avis pour une borne
   */
  getAvisByChargingStation(borneId: number): Observable<ApiResponse<Avis[]>> {
    return this.http.get<ApiResponse<Avis[]>>(`${this.apiUrl}/borne/${borneId}`);
  }

  /**
   * Récupère la note moyenne d'une borne
   */
  getAverageNote(borneId: number): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/borne/${borneId}/moyenne`);
  }

  /**
   * Récupère les avis de l'utilisateur connecté
   */
  getMyAvis(): Observable<ApiResponse<Avis[]>> {
    return this.http.get<ApiResponse<Avis[]>>(`${this.apiUrl}/mes-avis`);
  }

  /**
   * Crée un nouvel avis
   */
  createAvis(request: CreateAvisRequest): Observable<ApiResponse<Avis>> {
    return this.http.post<ApiResponse<Avis>>(this.apiUrl, request);
  }

  /**
   * Supprime un avis
   */
  deleteAvis(avisId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${avisId}`);
  }
}
