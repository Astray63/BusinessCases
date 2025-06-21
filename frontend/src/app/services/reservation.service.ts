import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Reservation } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  // Méthodes d'administration
  getAllReservations(): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<Reservation[]>>(this.apiUrl);
  }

  // Méthodes communes
  getReservationById(id: number): Observable<ApiResponse<Reservation>> {
    return this.http.get<ApiResponse<Reservation>>(`${this.apiUrl}/${id}`);
  }

  // Méthodes utilisateur
  /** Réservations de l'utilisateur connecté */
  getReservationsByCurrentUser(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<Reservation[]>>(`${this.apiUrl}/utilisateur/${userId}`);
  }

  getReservationsByUser(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<Reservation[]>>(`${this.apiUrl}/utilisateur/${userId}`);
  }

  getReservationsByBorne(borneId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<Reservation[]>>(`${this.apiUrl}/borne/${borneId}`);
  }

  createReservation(reservation: Partial<Reservation>): Observable<ApiResponse<Reservation>> {
    return this.http.post<ApiResponse<Reservation>>(this.apiUrl, reservation);
  }

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<ApiResponse<Reservation>> {
    // Non implémenté côté backend pour l'instant
    return this.http.put<ApiResponse<Reservation>>(`${this.apiUrl}/${id}`, reservation);
  }

  cancelReservation(id: number, requesterId?: number): Observable<ApiResponse<void>> {
    let params = new HttpParams();
    if (requesterId !== undefined) {
      params = params.set('requesterId', requesterId.toString());
    }
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${id}/cancel`, {}, { params });
  }

  completeReservation(id: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${id}/complete`, {});
  }

  // Export et factures
  getFacture(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/facture`, { responseType: 'blob' });
  }

  exportReservations(params: { userId: number, startDate?: Date, endDate?: Date }): Observable<Blob> {
    let httpParams = new HttpParams()
      .set('userId', params.userId.toString());

    if (params.startDate) {
      httpParams = httpParams.set('startDate', params.startDate.toISOString());
    }
    if (params.endDate) {
      httpParams = httpParams.set('endDate', params.endDate.toISOString());
    }

    return this.http.get(`${this.apiUrl}/export`, {
      params: httpParams,
      responseType: 'blob'
    });
  }
}