import { Injectable } from '@angular/core';
import { Observable, from, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface GeolocationPosition {
  lat: number;
  lng: number;
}

export interface GeolocationError {
  code: number;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class GeolocationService {
  private readonly DEFAULT_LOCATION: GeolocationPosition = { lat: 48.8566, lng: 2.3522 }; // Paris
  private readonly GEOLOCATION_TIMEOUT = 10000;

  constructor() {}

  getCurrentPosition(): Observable<GeolocationPosition> {
    if (!this.isGeolocationSupported()) {
      return of(this.DEFAULT_LOCATION);
    }

    return from(this.requestPosition()).pipe(
      catchError(() => of(this.DEFAULT_LOCATION))
    );
  }

  isGeolocationSupported(): boolean {
    return 'geolocation' in navigator;
  }

  private requestPosition(): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          reject(this.mapGeolocationError(error));
        },
        {
          enableHighAccuracy: true,
          timeout: this.GEOLOCATION_TIMEOUT,
          maximumAge: 0
        }
      );
    });
  }

  private mapGeolocationError(error: GeolocationPositionError): GeolocationError {
    let message = 'Unable to retrieve your position';
    
    switch (error.code) {
      case error.PERMISSION_DENIED:
        message = 'Location access denied. Using default location.';
        break;
      case error.POSITION_UNAVAILABLE:
        message = 'Position unavailable. Using default location.';
        break;
      case error.TIMEOUT:
        message = 'Location request timeout. Using default location.';
        break;
    }
    
    return {
      code: error.code,
      message
    };
  }

  calculateDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371; // Earth radius in km
    const dLat = this.deg2rad(lat2 - lat1);
    const dLon = this.deg2rad(lng2 - lng1);
    
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(lat1)) *
      Math.cos(this.deg2rad(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }
}
