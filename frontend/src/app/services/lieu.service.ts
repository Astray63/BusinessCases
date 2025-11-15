import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Lieu } from '../models/lieu.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class LieuService {
  private apiUrl = `${environment.apiUrl}/lieux`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Lieu[]> {
    return this.http.get<ApiResponse<Lieu[]>>(this.apiUrl).pipe(
      map(response => response.data || [])
    );
  }

  getLieux(): Observable<ApiResponse<Lieu[]>> {
    return this.http.get<ApiResponse<Lieu[]>>(this.apiUrl);
  }

  getById(id: number): Observable<Lieu> {
    return this.http.get<ApiResponse<Lieu>>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.data!)
    );
  }

  getByUtilisateur(userId: number): Observable<Lieu[]> {
    return this.http.get<ApiResponse<Lieu[]>>(`${this.apiUrl}/utilisateur/${userId}`).pipe(
      map(response => response.data || [])
    );
  }

  searchByNom(nom: string): Observable<Lieu[]> {
    const params = new HttpParams().set('nom', nom);
    return this.http.get<ApiResponse<Lieu[]>>(`${this.apiUrl}/search`, { params }).pipe(
      map(response => response.data || [])
    );
  }

  getProches(latitude: number, longitude: number, distance: number = 10): Observable<Lieu[]> {
    const params = new HttpParams()
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString())
      .set('distance', distance.toString());
    return this.http.get<ApiResponse<Lieu[]>>(`${this.apiUrl}/proches`, { params }).pipe(
      map(response => response.data || [])
    );
  }

  create(lieu: Lieu, userId: number): Observable<Lieu> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.post<ApiResponse<Lieu>>(this.apiUrl, lieu, { params }).pipe(
      map(response => response.data!)
    );
  }

  update(id: number, lieu: Lieu): Observable<Lieu> {
    return this.http.put<ApiResponse<Lieu>>(`${this.apiUrl}/${id}`, lieu).pipe(
      map(response => response.data!)
    );
  }

  delete(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${id}`);
  }
}
