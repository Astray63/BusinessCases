import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Reservation, ReservationBackend, ReservationFiltre, ReservationExportParams } from '../models/reservation.model';
import { ReservationMapperService } from './reservation-mapper.service';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = `${environment.apiUrl}/reservations`;

  constructor(
    private http: HttpClient,
    private mapper: ReservationMapperService
  ) {}

  // Admin methods
  getAllReservations(): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(this.apiUrl).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapArrayToFrontend(response.data) : []
      }))
    );
  }

  // Common methods
  getReservationById(id: number): Observable<ApiResponse<Reservation>> {
    return this.http.get<ApiResponse<ReservationBackend>>(`${this.apiUrl}/${id}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapToFrontend(response.data) : undefined
      }))
    );
  }

  // User methods - Client mode
  getReservationsByUser(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/utilisateur/${userId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapArrayToFrontend(response.data) : []
      }))
    );
  }

  getReservationsByBorne(borneId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/borne/${borneId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapArrayToFrontend(response.data) : []
      }))
    );
  }

  createReservation(reservation: Partial<Reservation>): Observable<ApiResponse<Reservation>> {
    return this.http.post<ApiResponse<Reservation>>(this.apiUrl, reservation);
  }

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<ApiResponse<Reservation>> {
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

  // Owner methods - Accept/Reject reservations
  accepterReservation(id: number, proprietaireId: number): Observable<ApiResponse<Reservation>> {
    return this.http.put<ApiResponse<ReservationBackend>>(`${this.apiUrl}/${id}/accepter`, { proprietaireId }).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapToFrontend(response.data) : undefined
      }))
    );
  }

  refuserReservation(id: number, proprietaireId: number, motif?: string): Observable<ApiResponse<Reservation>> {
    return this.http.put<ApiResponse<ReservationBackend>>(`${this.apiUrl}/${id}/refuser`, { 
      proprietaireId,
      motif 
    }).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapToFrontend(response.data) : undefined
      }))
    );
  }

  // Filter reservations
  getReservationsFiltrees(filtres: ReservationFiltre): Observable<ApiResponse<Reservation[]>> {
    let params = new HttpParams();
    
    if (filtres.statut) {
      params = params.set('statut', filtres.statut);
    }
    if (filtres.dateDebut) {
      params = params.set('dateDebut', filtres.dateDebut.toISOString());
    }
    if (filtres.dateFin) {
      params = params.set('dateFin', filtres.dateFin.toISOString());
    }
    if (filtres.borneId) {
      params = params.set('borneId', filtres.borneId.toString());
    }
    if (filtres.utilisateurId) {
      params = params.set('utilisateurId', filtres.utilisateurId.toString());
    }
    
    return this.http.get<ApiResponse<Reservation[]>>(`${this.apiUrl}/filtrer`, { params });
  }

  // Owner methods - Reservations on my bornes
  getReservationsProprietaire(proprietaireId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/proprietaire/${proprietaireId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapper.mapArrayToFrontend(response.data) : []
      }))
    );
  }

  // Export and invoices
  getFacture(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/facture`, { responseType: 'blob' });
  }

  exportReservations(params: ReservationExportParams): Observable<Blob> {
    let httpParams = new HttpParams();

    if (params.userId) {
      httpParams = httpParams.set('userId', params.userId.toString());
    }
    if (params.borneId) {
      httpParams = httpParams.set('borneId', params.borneId.toString());
    }
    if (params.statut) {
      httpParams = httpParams.set('statut', params.statut);
    }
    if (params.dateDebut) {
      httpParams = httpParams.set('dateDebut', params.dateDebut.toISOString());
    }
    if (params.dateFin) {
      httpParams = httpParams.set('dateFin', params.dateFin.toISOString());
    }

    return this.http.get(`${this.apiUrl}/export`, {
      params: httpParams,
      responseType: 'blob'
    });
  }

  genererRecuPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/recu-pdf`, { responseType: 'blob' });
  }

  downloadReceipt(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/receipt`, { responseType: 'blob' });
  }

  envoyerNotification(reservationId: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${reservationId}/notification`, {});
  }
}