import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { DashboardStats } from '../models/dashboard-stats.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère les statistiques du dashboard pour un utilisateur
   */
  getDashboardStats(userId: number): Observable<ApiResponse<DashboardStats>> {
    return this.http.get<ApiResponse<DashboardStats>>(`${this.apiUrl}/stats/${userId}`);
  }
}
