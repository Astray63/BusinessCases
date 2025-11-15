import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Utilisateur, UtilisateurAuth } from '../models/utilisateur.model';
import { ApiResponse } from '../models/api-response.model';

export interface AuthResponse {
  token: string;
  user: Utilisateur;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<Utilisateur | null>;
  public currentUser$: Observable<Utilisateur | null>;
  private apiUrl = `${environment.apiUrl}/auth`;
  private utilisateurApiUrl = `${environment.apiUrl}/utilisateurs`;

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<Utilisateur | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  login(credentials: UtilisateurAuth): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, credentials)
      .pipe(
        map(response => {
          if (response.result === 'SUCCESS' && response.data) {
            const expirationDate = new Date();
            expirationDate.setDate(expirationDate.getDate() + 7); // 7 jours
            
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('tokenExpiration', expirationDate.getTime().toString());
            localStorage.setItem('currentUser', JSON.stringify(response.data.user));
            this.currentUserSubject.next(response.data.user);
          }
          return response;
        })
      );
  }

  register(user: any, motDePasse: string): Observable<ApiResponse<AuthResponse>> {
    const registerRequest = {
      utilisateur: {
        nom: user.nom,
        prenom: user.prenom,
        pseudo: user.pseudo,
        email: user.email,
        dateNaissance: user.dateNaissance,
        role: 'client',
        iban: '',
        adressePhysique: '',
        medias: ''
      },
      motDePasse: motDePasse
    };

    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, registerRequest);
  }

  validateAccount(email: string, code: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/validate`, { email, code });
  }

  resendValidationCode(email: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/resend-code`, { email });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('tokenExpiration');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getAllUsers(): Observable<ApiResponse<Utilisateur[]>> {
    return this.http.get<ApiResponse<Utilisateur[]>>(`${this.apiUrl}/users`);
  }

  banUser(userId: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/users/${userId}/ban`, {});
  }

  unbanUser(userId: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/users/${userId}/unban`, {});
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): Utilisateur | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    const user = this.getCurrentUser();
    const expiration = localStorage.getItem('tokenExpiration');
    
    if (!token || !user) {
      return false;
    }
    
    // Vérifier si le token n'est pas expiré
    if (expiration) {
      const expirationDate = parseInt(expiration, 10);
      if (Date.now() > expirationDate) {
        this.logout();
        return false;
      }
    }
    
    return true;
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user ? user.role === 'admin' : false;
  }

  updateCurrentUser(updatedUser: Utilisateur): void {
    localStorage.setItem('currentUser', JSON.stringify(updatedUser));
    this.currentUserSubject.next(updatedUser);
  }

  ensureValidSession(): Observable<boolean> {
    const token = this.getToken();
    const storedUser = this.getCurrentUser();

    if (!token || !storedUser) {
      this.logout();
      return of(false);
    }

    const pseudo = encodeURIComponent(storedUser.pseudo);
    return this.http.get<ApiResponse<Utilisateur>>(`${this.utilisateurApiUrl}/pseudo/${pseudo}`)
      .pipe(
        map(response => {
          if (response.result === 'SUCCESS' && response.data) {
            this.updateCurrentUser(response.data);
            return true;
          }
          this.logout();
          return false;
        }),
        catchError(() => {
          this.logout();
          return of(false);
        })
      );
  }
}
