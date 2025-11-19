import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Signalement, CreateSignalementRequest, StatutSignalement } from '../models/signalement.model';

@Injectable({
  providedIn: 'root'
})
export class SignalementService {
  private apiUrl = `${environment.apiUrl}/signalements`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère tous les signalements pour une borne
   */
  getSignalementsByChargingStation(chargingStationId: number): Observable<ApiResponse<Signalement[]>> {
    return this.http.get<ApiResponse<Signalement[]>>(`${this.apiUrl}/borne/${chargingStationId}`);
  }

  /**
   * Récupère les signalements de l'utilisateur connecté
   */
  getMySignalements(): Observable<ApiResponse<Signalement[]>> {
    return this.http.get<ApiResponse<Signalement[]>>(`${this.apiUrl}/mes-signalements`);
  }

  /**
   * Récupère les signalements par statut
   */
  getSignalementsByStatut(statut: StatutSignalement): Observable<ApiResponse<Signalement[]>> {
    return this.http.get<ApiResponse<Signalement[]>>(`${this.apiUrl}/statut/${statut}`);
  }

  /**
   * Compte le nombre de signalements ouverts pour une borne
   */
  countOpenSignalements(chargingStationId: number): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/borne/${chargingStationId}/count`);
  }

  /**
   * Crée un nouveau signalement
   */
  createSignalement(request: CreateSignalementRequest): Observable<ApiResponse<Signalement>> {
    return this.http.post<ApiResponse<Signalement>>(this.apiUrl, request);
  }

  /**
   * Met à jour le statut d'un signalement
   */
  updateStatut(signalementId: number, statut: StatutSignalement): Observable<ApiResponse<Signalement>> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<ApiResponse<Signalement>>(`${this.apiUrl}/${signalementId}/statut`, null, { params });
  }

  /**
   * Supprime un signalement
   */
  deleteSignalement(signalementId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${signalementId}`);
  }
}
