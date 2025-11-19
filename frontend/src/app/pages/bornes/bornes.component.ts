import { Component, OnInit, OnDestroy, AfterViewInit, NgZone } from '@angular/core';
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
  private map: L.Map | undefined;
  private markers: L.Marker[] = [];
  private userMarker?: L.Marker;

  bornes: Borne[] = [];
  filteredBornes: Borne[] = [];
  loading = false;
  locating = false;
  showMap = true;
  
  // Modal
  selectedBorne: Borne | null = null;
  isModalOpen = false;
  
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
    private router: Router,
    private ngZone: NgZone
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
      this.map = undefined;
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

      // Defensive check: clean up any existing map instance
      if (this.map) {
        this.map.remove();
        this.map = undefined;
      }

      // Clean up container if it still has Leaflet class (defensive)
      if (mapElement.classList.contains('leaflet-container')) {
        // Force cleanup of internal Leaflet ID if present, though this is internal API
        // This helps when Angular reuses DOM elements or if remove() didn't clean up properly
        const el = mapElement as any;
        if (el._leaflet_id) {
          el._leaflet_id = null;
        }
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
      console.error('Error initializing map:', error);
      // If map initialization failed, reset state so we can try again
      this.mapInitialized = false;
      if (this.map) {
          try {
            this.map.remove();
          } catch (e) {}
          this.map = undefined;
      }
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
            .addTo(this.map!)
            .bindPopup(this.createPopupContent(borne));
          
          marker.on('click', () => {
            this.onBorneSelect(borne);
          });

          marker.on('popupopen', () => {
             const btnDetails = document.getElementById(`btn-details-${borne.idBorne}`);
             if (btnDetails) {
                 btnDetails.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.ngZone.run(() => this.openBorneDetails(borne));
                 });
             }
             
             const btnReserver = document.getElementById(`btn-reserver-${borne.idBorne}`);
             if (btnReserver) {
                 btnReserver.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.ngZone.run(() => this.reserverBorne(borne.idBorne!));
                 });
             }
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
    const isDisponible = borne.etat === 'DISPONIBLE';
    
    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge ${etatClass}">${etatLabel}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        <p class="mb-2"><strong>Distance:</strong> ${distance.toFixed(1)} km</p>
        <div style="display: flex; gap: 8px; margin-top: 8px;">
          <button 
            id="btn-reserver-${borne.idBorne}" 
            class="btn btn-sm w-100" 
            style="flex: 1; padding: 8px; background-color: ${isDisponible ? '#28a745' : '#6c757d'}; color: white; border: none; border-radius: 4px; cursor: ${isDisponible ? 'pointer' : 'not-allowed'}; font-weight: 500;"
            ${!isDisponible ? 'disabled' : ''}>
            <i class="bi bi-calendar-check"></i> Réserver
          </button>
          <button 
            id="btn-details-${borne.idBorne}" 
            class="btn btn-sm" 
            style="padding: 8px 12px; background-color: #0d6efd; color: white; border: none; border-radius: 4px; cursor: pointer;">
            <i class="bi bi-info-circle"></i>
          </button>
        </div>
      </div>
    `;
  }

  async searchBornes(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';

    // If search query is present, try to geocode it first
    if (this.searchQuery && this.searchQuery.trim().length > 2) {
      try {
        const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(this.searchQuery)}`);
        const data = await response.json();
        
        if (data && data.length > 0) {
          const newLat = parseFloat(data[0].lat);
          const newLng = parseFloat(data[0].lon);
          
          this.userLocation = {
            lat: newLat,
            lng: newLng
          };
          
          if (this.map) {
            this.map.setView([newLat, newLng], 12);
          }
          
          if (this.userMarker) {
            this.userMarker.setLatLng([newLat, newLng]);
            this.userMarker.bindPopup(`<strong>Position recherchée: ${this.searchQuery}</strong>`);
          }
        }
      } catch (e) {
        console.error('Erreur de géocodage', e);
      }
    }

    if (!this.userLocation) {
      return;
    }

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

      // Distance
      if (this.userLocation && borne.latitude && borne.longitude) {
        const dist = this.calculateDistance(borne.latitude, borne.longitude);
        if (dist > this.distance) {
          return false;
        }
      }

      // Price range
      const pMin = Number(this.prixMin);
      const pMax = Number(this.prixMax);
      if (borne.prix !== undefined && (borne.prix < pMin || borne.prix > pMax)) {
        return false;
      }

      // Power
      const powMin = Number(this.puissanceMin);
      if (borne.puissance !== undefined && borne.puissance < powMin) {
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
    
    if (this.showMap) {
      setTimeout(() => {
        if (!this.mapInitialized) {
          this.initMap();
        } else if (this.map) {
          this.map.invalidateSize();
        }
      }, 100);
    }
  }

  onBorneSelect(borne: Borne): void {
    if (!borne.latitude || !borne.longitude) return;

    if (!this.showMap) {
      this.showMap = true;
      setTimeout(() => {
        if (!this.mapInitialized) {
          this.initMap();
        } else if (this.map) {
          this.map.invalidateSize();
        }
        
        if (this.map) {
          this.map.setView([borne.latitude!, borne.longitude!], 15);
        }
      }, 200);
    } else {
      if (this.map) {
        this.map.setView([borne.latitude, borne.longitude], 15);
      }
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

  openBorneDetails(borne: Borne): void {
    this.selectedBorne = borne;
    this.isModalOpen = true;
  }

  closeBorneDetails(): void {
    this.isModalOpen = false;
    this.selectedBorne = null;
  }
}
