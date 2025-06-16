import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Borne, BorneFiltre } from '../models/borne.model';

@Injectable({
  providedIn: 'root'
})
export class BorneService {
  private apiUrl = `${environment.apiUrl}/bornes`;

  constructor(private http: HttpClient) {}

  // Méthodes publiques
  getBornesProches(latitude: number, longitude: number, rayon: number): Observable<ApiResponse<Borne[]>> {
    const params = new HttpParams()
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString())
      .set('rayon', rayon.toString());
    
    return this.http.get<ApiResponse<Borne[]>>(`${this.apiUrl}/proches`, { params });
  }

  getBornesDisponibles(filtres?: BorneFiltre): Observable<ApiResponse<Borne[]>> {
    let params = new HttpParams();
    if (filtres) {
      Object.entries(filtres).forEach(([key, value]) => {
        if (value !== undefined) {
          params = params.append(key, value.toString());
        }
      });
    }
    return this.http.get<ApiResponse<Borne[]>>(`${this.apiUrl}/disponibles`, { params });
  }

  getBorneById(id: number): Observable<ApiResponse<Borne>> {
    return this.http.get<ApiResponse<Borne>>(`${this.apiUrl}/${id}`);
  }

  // Méthodes d'administration
  getAllBornes(): Observable<ApiResponse<Borne[]>> {
    return this.http.get<ApiResponse<Borne[]>>(this.apiUrl);
  }

  createBorne(borne: Partial<Borne>): Observable<ApiResponse<Borne>> {
    return this.http.post<ApiResponse<Borne>>(this.apiUrl, borne);
  }

  updateBorne(id: number, borne: Partial<Borne>): Observable<ApiResponse<Borne>> {
    return this.http.put<ApiResponse<Borne>>(`${this.apiUrl}/${id}`, borne);
  }

  deleteBorne(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  // Gestion des états
  marquerHorsService(id: number): Observable<ApiResponse<Borne>> {
    return this.http.put<ApiResponse<Borne>>(`${this.apiUrl}/${id}/hors-service`, {});
  }

  marquerEnService(id: number): Observable<ApiResponse<Borne>> {
    return this.http.put<ApiResponse<Borne>>(`${this.apiUrl}/${id}/en-service`, {});
  }
}