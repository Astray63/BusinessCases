import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface VerifyEmailRequest {
  email: string;
  code: string;
}

export interface ResendVerificationRequest {
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmailVerificationService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  verifyEmail(request: VerifyEmailRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-email`, request);
  }

  resendVerificationCode(request: ResendVerificationRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/resend-verification`, request);
  }
}
