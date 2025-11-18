import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { BorneService } from '../../services/borne.service';
import { Borne } from '../../models/borne.model';
import { ApiResponse } from '../../models/api-response.model';
import * as L from 'leaflet';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  private map!: L.Map;
  private markers: L.Marker[] = [];
  private userMarker?: L.Marker;
  
  isLoggedIn = false;
  bornesPubliques: Borne[] = [];
  isLoading = false;
  userLocation: { lat: number; lng: number } | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private borneService: BorneService
  ) {
    // Créer une référence globale pour que le popup puisse appeler la méthode de réservation
    (window as any).reserveBorne = (borneId: number) => {
      this.navigateToReservation(this.bornesPubliques.find(b => b.idBorne === borneId)!);
    };
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.getUserLocationAsync();
  }

  ngAfterViewInit(): void {
    // Map will be initialized once geolocation is obtained
  }

  private async getUserLocationAsync(): Promise<void> {
    try {
      this.userLocation = await this.requestGeolocation();
    } catch (error: any) {
      // Fallback to default location (Paris)
      this.userLocation = { lat: 48.8566, lng: 2.3522 };
    }
    
    this.initMap();
    this.loadBornesPubliques();
  }

  private requestGeolocation(): Promise<{ lat: number; lng: number }> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation not supported'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      );
    });
  }

  getUserLocation(): void {
    // Deprecated - using getUserLocationAsync() instead
  }

  initMap(): void {
    if (!this.userLocation) {
      return;
    }

    try {
      const mapElement = document.getElementById('public-map');
      if (!mapElement) {
        return;
      }

      if (this.map) {
        this.map.remove();
      }

      this.map = L.map('public-map').setView(
        [this.userLocation.lat, this.userLocation.lng],
        12
      );

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      const userIcon = L.divIcon({
        html: '<div class="user-marker"><i class="bi bi-geo-alt-fill"></i></div>',
        className: 'custom-marker-user',
        iconSize: [32, 32],
        iconAnchor: [16, 32]
      });

      this.userMarker = L.marker([this.userLocation.lat, this.userLocation.lng], { icon: userIcon })
        .addTo(this.map)
        .bindPopup('<strong>Votre position</strong>')
        .openPopup();

      this.updateMapMarkers();
    } catch (error) {
      // Silent fail - map initialization error
    }
  }

  updateMapMarkers(): void {
    if (!this.map) {
      return;
    }

    // Remove old markers
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Add new markers for each borne
    this.bornesPubliques.forEach((borne) => {
      if (borne.latitude && borne.longitude) {
        try {
          const icon = this.getBorneIcon(borne.etat);
          
          const marker = L.marker([borne.latitude, borne.longitude], { icon })
            .addTo(this.map)
            .bindPopup(this.createPopupContent(borne));
          
          marker.on('click', () => {
            this.selectBorne(borne);
          });
          
          this.markers.push(marker);
        } catch (error) {
          // Silent fail for individual marker
        }
      }
    });

    // Adjust view to show all markers
    if (this.markers.length > 0 && this.userMarker) {
      const group = L.featureGroup([...this.markers, this.userMarker]);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  getBorneIcon(etat: string): L.DivIcon {
    let color = '#28a745'; // vert par défaut
    
    switch(etat) {
      case 'DISPONIBLE':
        color = '#28a745'; // vert
        break;
      case 'OCCUPE':
        color = '#ffc107'; // jaune
        break;
      default:
        color = '#dc3545'; // rouge
        break;
    }

    return L.divIcon({
      html: `<div class="borne-marker" style="background-color: ${color};"><i class="bi bi-lightning-charge-fill"></i></div>`,
      className: 'custom-marker-borne',
      iconSize: [32, 32],
      iconAnchor: [16, 32],
      popupAnchor: [0, -32]
    });
  }

  createPopupContent(borne: Borne): string {
    const etatLabel = borne.etat === 'DISPONIBLE' ? 'Disponible' : (borne.etat === 'OCCUPE' ? 'Occupée' : 'Hors service');
    const etatClass = borne.etat === 'DISPONIBLE' ? 'success' : (borne.etat === 'OCCUPE' ? 'warning' : 'danger');
    const prix = borne.prix ? `${borne.prix}€/h` : 'N/A';
    const distance = this.userLocation ? this.calculateDistance(borne.latitude!, borne.longitude!) : 0;
    
    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge badge-${etatClass}">${etatLabel}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        <p class="mb-2"><strong>Distance:</strong> ${distance.toFixed(1)} km</p>
        <div class="popup-actions">
          <button class="btn btn-sm btn-primary" onclick="window.reserveBorne(${borne.idBorne})">
            <i class="bi bi-calendar-check"></i> Réserver
          </button>
        </div>
      </div>
    `;
  }

  loadBornesPubliques(): void {
    this.isLoading = true;
    this.borneService.getBornesDisponibles().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.bornesPubliques = response.data || [];
        this.isLoading = false;
        
        if (this.map) {
          this.updateMapMarkers();
        }
      },
      error: (error: any) => {
        this.isLoading = false;
      }
    });
  }

  selectBorne(borne: Borne): void {
    // Centrer la carte sur la borne sélectionnée
    if (borne.latitude && borne.longitude && this.map) {
      this.map.setView([borne.latitude, borne.longitude], 15);
    }
  }

  centerMapOnBorne(borne: Borne): void {
    this.selectBorne(borne);
    // Scroll jusqu'à la carte
    document.getElementById('public-map')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  calculateDistance(lat: number, lng: number): number {
    if (!this.userLocation) return 0;

    const R = 6371; // Rayon de la Terre en km
    const dLat = this.deg2rad(lat - this.userLocation.lat);
    const dLon = this.deg2rad(lng - this.userLocation.lng);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(this.userLocation.lat)) *
      Math.cos(this.deg2rad(lat)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    return distance;
  }

  deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  navigateToAction(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/auth/register']);
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
    // Nettoyer la référence globale
    delete (window as any).reserveBorne;
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
} 