import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { BorneService } from '../../services/borne.service';
import { GeolocationService, GeolocationPosition } from '../../services/geolocation.service';
import { MapService } from '../../services/map.service';
import { Borne } from '../../models/borne.model';
import { ApiResponse } from '../../models/api-response.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly MAP_ID = 'public-map';
  
  isLoggedIn = false;
  bornesPubliques: Borne[] = [];
  isLoading = false;
  userLocation: GeolocationPosition | null = null;
  selectedBorne: Borne | null = null;
  isModalOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private borneService: BorneService,
    private geolocationService: GeolocationService,
    private mapService: MapService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.getUserLocation();
  }

  ngAfterViewInit(): void {
    if (this.userLocation) {
      setTimeout(() => this.initMap(), 100);
    }
  }

  private getUserLocation(): void {
    this.geolocationService.getCurrentPosition().subscribe({
      next: (position) => {
        this.userLocation = position;
        this.initMap();
        this.loadBornesPubliques();
      },
      error: () => {
        this.userLocation = { lat: 48.8566, lng: 2.3522 };
        this.initMap();
        this.loadBornesPubliques();
      }
    });
  }

  initMap(): void {
    if (!this.userLocation) {
      return;
    }

    try {
      const mapElement = document.getElementById(this.MAP_ID);
      if (!mapElement) {
        return;
      }

      this.mapService.initializeMap({
        containerId: this.MAP_ID,
        center: this.userLocation,
        zoom: 12
      });

      this.mapService.addUserMarker(this.MAP_ID, this.userLocation, 'Votre position');
      this.updateMapMarkers();
    } catch (error) {
      // Silent fail
    }
  }

  updateMapMarkers(): void {
    // Calculer la distance pour chaque borne
    const bornesAvecDistance = this.bornesPubliques.map(borne => ({
      ...borne,
      distance: borne.latitude && borne.longitude 
        ? this.calculateDistance(borne.latitude, borne.longitude)
        : undefined
    }));

    this.mapService.addBorneMarkers(
      this.MAP_ID,
      bornesAvecDistance,
      (borne) => this.selectBorne(borne)
    );
  }

  loadBornesPubliques(): void {
    this.isLoading = true;
    this.borneService.getBornesDisponibles().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.bornesPubliques = response.data || [];
        this.isLoading = false;
        this.updateMapMarkers();
      },
      error: (error: any) => {
        this.isLoading = false;
      }
    });
  }

  selectBorne(borne: Borne): void {
    if (borne.latitude && borne.longitude) {
      this.mapService.setView(this.MAP_ID, {
        lat: borne.latitude,
        lng: borne.longitude
      }, 15);
    }
  }

  centerMapOnBorne(borne: Borne): void {
    this.selectBorne(borne);
    document.getElementById(this.MAP_ID)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  calculateDistance(lat: number, lng: number): number {
    if (!this.userLocation) return 0;
    return this.geolocationService.calculateDistance(
      this.userLocation.lat,
      this.userLocation.lng,
      lat,
      lng
    );
  }

  navigateToAction(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/dashboard']);
    } else {
      window.location.href = '/assets/auth/register.html';
    }
  }

  ngOnDestroy(): void {
    this.mapService.destroyMap(this.MAP_ID);
  }

  navigateToReservation(borne: Borne): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/client/mes-reservations'], { 
        queryParams: { borneId: borne.idBorne } 
      });
    } else {
      this.router.navigate(['/auth/login'], { 
        queryParams: { returnUrl: '/client/mes-reservations', borneId: borne.idBorne } 
      });
    }
  }

  openBorneDetails(borne: Borne): void {
    this.selectedBorne = borne;
    this.isModalOpen = true;
  }

  closeBorneDetails(): void {
    this.isModalOpen = false;
    this.selectedBorne = null;
  }
}