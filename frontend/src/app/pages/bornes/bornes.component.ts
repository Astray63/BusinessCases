import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { BorneService } from '../../services/borne.service';
import { AuthService } from '../../services/auth.service';
import { Borne } from '../../models/borne.model';
import * as L from 'leaflet';

@Component({
  selector: 'app-bornes',
  templateUrl: './bornes.component.html'
})
export class BornesComponent implements OnInit, OnDestroy, AfterViewInit {
  private map!: L.Map;
  private markers: L.Marker[] = [];
  private userMarker?: L.Marker;

  bornes: Borne[] = [];
  filteredBornes: Borne[] = [];
  loading = false;
  locating = false;
  showMap = true;
  
  // Filters
  searchQuery = '';
  distance = 50;
  prixMin = 0;
  prixMax = 50;
  puissanceMin = 0;
  selectedEtat = 'all';
  disponibleOnly = false;
  
  // Geolocation
  userLocation: { lat: number; lng: number } | null = null;
  geolocationStatus: 'pending' | 'success' | 'error' = 'pending';
  geolocationMessage = '';
  errorMessage = '';
  
  private mapInitialized = false;

  constructor(
    private borneService: BorneService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getUserLocation();
  }

  ngAfterViewInit(): void {
    if (this.showMap && this.userLocation) {
      setTimeout(() => this.initMap(), 100);
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  getUserLocation(): void {
    if (!navigator.geolocation) {
      this.handleGeolocationError('La géolocalisation n\'est pas supportée par votre navigateur');
      return;
    }

    this.locating = true;
    this.geolocationStatus = 'pending';

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.userLocation = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        this.locating = false;
        this.geolocationStatus = 'success';
        
        if (this.showMap && !this.mapInitialized) {
          this.initMap();
        }
        
        this.searchBornes();
      },
      (error) => {
        let message = 'Impossible de récupérer votre position';
        
        switch (error.code) {
          case error.PERMISSION_DENIED:
            message = 'Vous avez refusé l\'accès à la géolocalisation. Les résultats peuvent être limités.';
            break;
          case error.POSITION_UNAVAILABLE:
            message = 'Votre position est indisponible. Utilisation de la position par défaut.';
            break;
          case error.TIMEOUT:
            message = 'La demande de géolocalisation a expiré. Utilisation de la position par défaut.';
            break;
        }
        
        this.handleGeolocationError(message);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  }

  private handleGeolocationError(message: string): void {
    this.geolocationMessage = message;
    this.geolocationStatus = 'error';
    this.locating = false;
    
    // Fallback to default location (Paris)
    this.userLocation = { lat: 48.8566, lng: 2.3522 };
    
    if (this.showMap && !this.mapInitialized) {
      this.initMap();
    }
    
    this.searchBornes();
  }

  retryGeolocation(): void {
    this.geolocationMessage = '';
    this.errorMessage = '';
    this.getUserLocation();
  }

  initMap(): void {
    if (!this.userLocation || this.mapInitialized) {
      return;
    }

    try {
      const mapElement = document.getElementById('map');
      if (!mapElement) {
        return;
      }

      if (this.map) {
        this.map.remove();
      }

      this.map = L.map('map').setView(
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
        .bindPopup('<strong>Votre position</strong>');

      this.mapInitialized = true;
      this.updateMapMarkers();
    } catch (error) {
      // Silent fail
    }
  }

  updateMapMarkers(): void {
    if (!this.map || !this.mapInitialized) {
      return;
    }

    // Remove old markers
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Add markers for filtered bornes
    this.filteredBornes.forEach((borne) => {
      if (borne.latitude && borne.longitude) {
        try {
          const icon = this.getBorneIcon(borne.etat);
          
          const marker = L.marker([borne.latitude, borne.longitude], { icon })
            .addTo(this.map)
            .bindPopup(this.createPopupContent(borne));
          
          marker.on('click', () => {
            this.onBorneSelect(borne);
          });
          
          this.markers.push(marker);
        } catch (error) {
          // Silent fail
        }
      }
    });

    // Adjust view
    if (this.markers.length > 0 && this.userMarker) {
      const group = L.featureGroup([...this.markers, this.userMarker]);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  getBorneIcon(etat: string): L.DivIcon {
    let color = '#28a745';
    
    switch(etat) {
      case 'DISPONIBLE':
        color = '#28a745';
        break;
      case 'OCCUPE':
        color = '#ffc107';
        break;
      default:
        color = '#dc3545';
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
    const etatLabel = this.getEtatLabel(borne.etat);
    const etatClass = this.getEtatBadgeClass(borne.etat);
    const prix = borne.prix ? `${borne.prix}€/h` : 'N/A';
    const distance = this.userLocation ? this.calculateDistance(borne.latitude!, borne.longitude!) : 0;
    
    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge ${etatClass}">${etatLabel}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        <p class="mb-2"><strong>Distance:</strong> ${distance.toFixed(1)} km</p>
      </div>
    `;
  }

  searchBornes(): void {
    if (!this.userLocation) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.borneService.getBornesProches(
      this.userLocation.lat,
      this.userLocation.lng,
      this.distance
    ).subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          this.bornes = response.data;
          this.applyFilters();
        }
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors de la recherche des bornes';
        this.loading = false;
      }
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredBornes = this.bornes.filter(borne => {
      // Search query
      if (this.searchQuery && !borne.localisation.toLowerCase().includes(this.searchQuery.toLowerCase())) {
        return false;
      }

      // Price range
      if (borne.prix && (borne.prix < this.prixMin || borne.prix > this.prixMax)) {
        return false;
      }

      // Power
      if (borne.puissance && borne.puissance < this.puissanceMin) {
        return false;
      }

      // State
      if (this.selectedEtat !== 'all' && borne.etat !== this.selectedEtat) {
        return false;
      }

      // Available only
      if (this.disponibleOnly && borne.etat !== 'DISPONIBLE') {
        return false;
      }

      return true;
    });

    if (this.showMap && this.mapInitialized) {
      this.updateMapMarkers();
    }
  }

  toggleView(): void {
    this.showMap = !this.showMap;
    
    if (this.showMap && !this.mapInitialized && this.userLocation) {
      setTimeout(() => this.initMap(), 100);
    }
  }

  onBorneSelect(borne: Borne): void {
    if (this.showMap && borne.latitude && borne.longitude && this.map) {
      this.map.setView([borne.latitude, borne.longitude], 15);
    } else {
      this.showMap = true;
      setTimeout(() => {
        if (this.map && borne.latitude && borne.longitude) {
          this.map.setView([borne.latitude, borne.longitude], 15);
        }
      }, 200);
    }
  }

  reserverBorne(borneId: number): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/client/mes-reservations'], { 
        queryParams: { borneId } 
      });
    } else {
      this.router.navigate(['/auth/login'], { 
        queryParams: { returnUrl: '/client/mes-reservations', borneId } 
      });
    }
  }

  calculateDistance(lat: number, lng: number): number {
    if (!this.userLocation) return 0;

    const R = 6371;
    const dLat = this.deg2rad(lat - this.userLocation.lat);
    const dLon = this.deg2rad(lng - this.userLocation.lng);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(this.userLocation.lat)) *
      Math.cos(this.deg2rad(lat)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  getEtatLabel(etat: string): string {
    const labels: { [key: string]: string } = {
      'DISPONIBLE': 'Disponible',
      'OCCUPE': 'Occupée',
      'HORS_SERVICE': 'Hors service',
      'MAINTENANCE': 'En maintenance'
    };
    return labels[etat] || etat;
  }

  getEtatBadgeClass(etat: string): string {
    const classes: { [key: string]: string } = {
      'DISPONIBLE': 'badge-success',
      'OCCUPE': 'badge-warning',
      'HORS_SERVICE': 'badge-danger',
      'MAINTENANCE': 'badge-secondary'
    };
    return classes[etat] || 'badge-secondary';
  }
}
