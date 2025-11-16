import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Reservation, ReservationBackend, ReservationFiltre, ReservationExportParams } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  // Helper method to map backend response to frontend model
  private mapBackendToFrontend(backend: ReservationBackend): Reservation {
    return {
      idReservation: backend.id,
      utilisateur: backend.utilisateur!,
      borne: backend.borne!,
      dateDebut: new Date(backend.dateDebut),
      dateFin: new Date(backend.dateFin),
      statut: this.mapEtatToStatut(backend.etat),
      montantTotal: backend.totalPrice
    };
  }

  private mapEtatToStatut(etat: string): any {
    const mapping: { [key: string]: string } = {
      'ACTIVE': 'CONFIRMEE',
      'TERMINEE': 'TERMINEE',
      'ANNULEE': 'ANNULEE',
      'CONFIRMEE': 'CONFIRMEE',
      'EN_ATTENTE': 'EN_ATTENTE',
      'REFUSEE': 'REFUSEE'
    };
    return mapping[etat] || etat;
  }

  // Méthodes d'administration
  getAllReservations(): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(this.apiUrl).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.map(r => this.mapBackendToFrontend(r)) : []
      }))
    );
  }

  // Méthodes communes
  getReservationById(id: number): Observable<ApiResponse<Reservation>> {
    return this.http.get<ApiResponse<ReservationBackend>>(`${this.apiUrl}/${id}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? this.mapBackendToFrontend(response.data) : undefined
      }))
    );
  }

  // Méthodes utilisateur
  /** 🔵 MODE CLIENT: Réservations faites par l'utilisateur (en tant que client) */
  getMesReservationsClient(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/utilisateur/${userId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.map(r => this.mapBackendToFrontend(r)) : []
      }))
    );
  }

  /** Réservations de l'utilisateur connecté */
  getReservationsByCurrentUser(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.getMesReservationsClient(userId);
  }

  getReservationsByUser(userId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/utilisateur/${userId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.map(r => this.mapBackendToFrontend(r)) : []
      }))
    );
  }

  getReservationsByBorne(borneId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<ReservationBackend[]>>(`${this.apiUrl}/borne/${borneId}`).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.map(r => this.mapBackendToFrontend(r)) : []
      }))
    );
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

  // Accepter / refuser une réservation (propriétaire)
  accepterReservation(id: number, proprietaireId: number): Observable<ApiResponse<Reservation>> {
    return this.http.put<ApiResponse<Reservation>>(`${this.apiUrl}/${id}/accepter`, { proprietaireId });
  }

  refuserReservation(id: number, proprietaireId: number, motif?: string): Observable<ApiResponse<Reservation>> {
    return this.http.put<ApiResponse<Reservation>>(`${this.apiUrl}/${id}/refuser`, { 
      proprietaireId,
      motif 
    });
  }

  // Filtrer les réservations
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

  // Réservations pour un propriétaire de borne
  /** 🟢 MODE PROPRIÉTAIRE: Toutes les réservations reçues sur mes bornes */
  getMesReservationsProprietaire(proprietaireId: number): Observable<ApiResponse<Reservation[]>> {
    return this.http.get<ApiResponse<Reservation[]>>(`${this.apiUrl}/proprietaire/${proprietaireId}`);
  }

  /** 🟢 MODE PROPRIÉTAIRE: Demandes en attente sur mes bornes */
  getDemandesEnAttente(proprietaireId: number): Observable<ApiResponse<Reservation[]>> {
    return this.getMesReservationsProprietaire(proprietaireId).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.filter(r => r.statut === 'EN_ATTENTE') : []
      }))
    );
  }

  /** 🟢 MODE PROPRIÉTAIRE: Historique des réservations sur mes bornes */
  getHistoriqueReservationsProprietaire(proprietaireId: number): Observable<ApiResponse<Reservation[]>> {
    return this.getMesReservationsProprietaire(proprietaireId).pipe(
      map(response => ({
        ...response,
        data: response.data ? response.data.filter(r => 
          r.statut !== 'EN_ATTENTE'
        ) : []
      }))
    );
  }

  getReservationsProprietaire(proprietaireId: number): Observable<ApiResponse<Reservation[]>> {
    return this.getMesReservationsProprietaire(proprietaireId);
  }

  // Export et factures
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

  // Générer un reçu PDF
  genererRecuPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/recu-pdf`, { responseType: 'blob' });
  }

  // Envoyer une notification
  envoyerNotification(reservationId: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${reservationId}/notification`, {});
  }
}