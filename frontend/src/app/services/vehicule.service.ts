import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { Vehicule } from '../models/vehicule.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VehiculeService {
  private apiUrl = `${environment.apiUrl}/vehicules`;

  constructor(private http: HttpClient) { }

  getVehicules(): Observable<ApiResponse<Vehicule[]>> {
    return this.http.get<ApiResponse<Vehicule[]>>(this.apiUrl);
  }

  addVehicule(vehicule: Omit<Vehicule, 'idVehicule'>): Observable<ApiResponse<Vehicule>> {
    return this.http.post<ApiResponse<Vehicule>>(this.apiUrl, vehicule);
  }

  updateVehicule(id: number, vehicule: Partial<Vehicule>): Observable<ApiResponse<Vehicule>> {
    return this.http.put<ApiResponse<Vehicule>>(`${this.apiUrl}/${id}`, vehicule);
  }

  deleteVehicule(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
