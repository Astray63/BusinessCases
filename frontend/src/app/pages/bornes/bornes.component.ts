import { Component, OnInit, OnDestroy, AfterViewInit, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { BorneService } from '../../services/borne.service';
import { AuthService } from '../../services/auth.service';
import { GeolocationService, GeolocationPosition } from '../../services/geolocation.service';
import { Borne } from '../../models/borne.model';
import * as L from 'leaflet';

@Component({
  selector: 'app-bornes',
  templateUrl: './bornes.component.html'
})
export class BornesComponent implements OnInit, OnDestroy, AfterViewInit {
  private map?: L.Map;
  private markers: L.Marker[] = [];
  private userMarker?: L.Marker;
  private mapInitialized = false;

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
  userLocation: GeolocationPosition | null = null;
  geolocationStatus: 'pending' | 'success' | 'error' = 'pending';
  geolocationMessage = '';
  errorMessage = '';

  // Labels et badges
  private readonly etatLabels: Record<string, string> = {
    'DISPONIBLE': 'Disponible',
    'OCCUPE': 'Occupée',
    'HORS_SERVICE': 'Hors service',
    'MAINTENANCE': 'En maintenance'
  };

  private readonly etatBadges: Record<string, string> = {
    'DISPONIBLE': 'badge-success',
    'OCCUPE': 'badge-warning',
    'HORS_SERVICE': 'badge-danger',
    'MAINTENANCE': 'badge-secondary'
  };

  constructor(
    private borneService: BorneService,
    private authService: AuthService,
    private geolocationService: GeolocationService,
    private router: Router,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this.getUserLocation();
  }

  ngAfterViewInit(): void {
    if (this.showMap && this.userLocation) {
      setTimeout(() => this.initMap(), 100);
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
    this.map = undefined;
  }

  // ============ GEOLOCATION ============

  getUserLocation(): void {
    this.locating = true;
    this.geolocationStatus = 'pending';

    this.geolocationService.getCurrentPosition().subscribe({
      next: (position) => {
        this.userLocation = position;
        this.locating = false;
        this.geolocationStatus = 'success';
        this.initMapIfNeeded();
        this.searchBornes();
      },
      error: (err) => {
        this.handleGeolocationError(err.message || 'Impossible de récupérer votre position');
      }
    });
  }

  private handleGeolocationError(message: string): void {
    this.geolocationMessage = message;
    this.geolocationStatus = 'error';
    this.locating = false;
    this.userLocation = { lat: 48.8566, lng: 2.3522 }; // Paris par défaut
    this.initMapIfNeeded();
    this.searchBornes();
  }

  retryGeolocation(): void {
    this.geolocationMessage = '';
    this.errorMessage = '';
    this.getUserLocation();
  }

  // ============ MAP ============

  private initMapIfNeeded(): void {
    if (this.showMap && !this.mapInitialized) {
      this.initMap();
    }
  }

  initMap(): void {
    if (!this.userLocation || this.mapInitialized) return;

    const mapElement = document.getElementById('map');
    if (!mapElement) return;

    // Cleanup existing map
    this.cleanupMap(mapElement);

    try {
      this.map = L.map('map').setView([this.userLocation.lat, this.userLocation.lng], 12);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      this.addUserMarker();
      this.mapInitialized = true;
      this.updateMapMarkers();
    } catch {
      this.mapInitialized = false;
      this.map?.remove();
      this.map = undefined;
    }
  }

  private cleanupMap(element: HTMLElement): void {
    this.map?.remove();
    this.map = undefined;
    if (element.classList.contains('leaflet-container')) {
      (element as any)._leaflet_id = null;
    }
  }

  private addUserMarker(): void {
    if (!this.map || !this.userLocation) return;

    const icon = L.divIcon({
      html: '<div class="user-marker"><i class="bi bi-geo-alt-fill"></i></div>',
      className: 'custom-marker-user',
      iconSize: [32, 32],
      iconAnchor: [16, 32]
    });

    this.userMarker = L.marker([this.userLocation.lat, this.userLocation.lng], { icon })
      .addTo(this.map)
      .bindPopup('<strong>Votre position</strong>');
  }

  updateMapMarkers(): void {
    if (!this.map || !this.mapInitialized) return;

    // Clear old markers
    this.markers.forEach(m => m.remove());
    this.markers = [];

    // Add new markers
    this.filteredBornes.forEach(borne => {
      if (borne.latitude && borne.longitude) {
        const marker = this.createBorneMarker(borne);
        if (marker) this.markers.push(marker);
      }
    });

    this.fitMapBounds();
  }

  private createBorneMarker(borne: Borne): L.Marker | null {
    if (!this.map) return null;

    const icon = this.getBorneIcon(borne.etat);
    const marker = L.marker([borne.latitude!, borne.longitude!], { icon })
      .addTo(this.map)
      .bindPopup(this.createPopupContent(borne));

    marker.on('click', () => this.onBorneSelect(borne));
    marker.on('popupopen', () => this.bindPopupEvents(borne));

    return marker;
  }

  private bindPopupEvents(borne: Borne): void {
    document.getElementById(`btn-details-${borne.idBorne}`)?.addEventListener('click', (e) => {
      e.stopPropagation();
      this.ngZone.run(() => this.openBorneDetails(borne));
    });

    document.getElementById(`btn-reserver-${borne.idBorne}`)?.addEventListener('click', (e) => {
      e.stopPropagation();
      this.ngZone.run(() => this.reserverBorne(borne.idBorne!));
    });
  }

  private fitMapBounds(): void {
    if (this.markers.length > 0 && this.userMarker && this.map) {
      const group = L.featureGroup([...this.markers, this.userMarker]);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  getBorneIcon(etat: string): L.DivIcon {
    const colors: Record<string, string> = {
      'DISPONIBLE': '#28a745',
      'OCCUPE': '#ffc107'
    };
    const color = colors[etat] || '#dc3545';

    return L.divIcon({
      html: `<div class="borne-marker" style="background-color: ${color};"><i class="bi bi-lightning-charge-fill"></i></div>`,
      className: 'custom-marker-borne',
      iconSize: [32, 32],
      iconAnchor: [16, 32],
      popupAnchor: [0, -32]
    });
  }

  createPopupContent(borne: Borne): string {
    const distance = this.userLocation ? this.calculateDistance(borne.latitude!, borne.longitude!) : 0;
    const isDisponible = borne.etat === 'DISPONIBLE';
    const btnStyle = isDisponible
      ? 'background-color: #28a745; cursor: pointer;'
      : 'background-color: #6c757d; cursor: not-allowed;';

    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge ${this.getEtatBadgeClass(borne.etat)}">${this.getEtatLabel(borne.etat)}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${borne.prix ? borne.prix + '€/h' : 'N/A'}</p>
        <p class="mb-2"><strong>Distance:</strong> ${distance.toFixed(1)} km</p>
        <div style="display: flex; gap: 8px; margin-top: 8px;">
          <button id="btn-reserver-${borne.idBorne}" class="btn btn-sm w-100" 
            style="flex: 1; padding: 8px; ${btnStyle} color: white; border: none; border-radius: 4px; font-weight: 500;"
            ${!isDisponible ? 'disabled' : ''}>
            <i class="bi bi-calendar-check"></i> Réserver
          </button>
          <button id="btn-details-${borne.idBorne}" class="btn btn-sm" 
            style="padding: 8px 12px; background-color: #0d6efd; color: white; border: none; border-radius: 4px; cursor: pointer;">
            <i class="bi bi-info-circle"></i>
          </button>
        </div>
      </div>`;
  }

  // ============ SEARCH & FILTERS ============

  async searchBornes(): Promise<void> {
    if (!this.userLocation) return;

    this.loading = true;
    this.errorMessage = '';

    await this.geocodeSearchQuery();

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
      error: () => {
        this.errorMessage = 'Erreur lors de la recherche des bornes';
        this.loading = false;
      }
    });
  }

  private async geocodeSearchQuery(): Promise<void> {
    if (!this.searchQuery?.trim() || this.searchQuery.length <= 2) return;

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(this.searchQuery)}`
      );
      const data = await response.json();

      if (data?.length > 0) {
        const newLocation = { lat: parseFloat(data[0].lat), lng: parseFloat(data[0].lon) };
        this.userLocation = newLocation;
        this.map?.setView([newLocation.lat, newLocation.lng], 12);
        this.userMarker?.setLatLng([newLocation.lat, newLocation.lng])
          .bindPopup(`<strong>Position recherchée: ${this.searchQuery}</strong>`);
      }
    } catch { /* ignore geocoding errors */ }
  }

  onSearchChange(): void { this.applyFilters(); }
  onFilterChange(): void { this.applyFilters(); }

  applyFilters(): void {
    this.filteredBornes = this.bornes.filter(borne => {
      if (this.searchQuery && !borne.localisation.toLowerCase().includes(this.searchQuery.toLowerCase())) return false;
      if (this.userLocation && borne.latitude && borne.longitude) {
        if (this.calculateDistance(borne.latitude, borne.longitude) > this.distance) return false;
      }
      if (borne.prix !== undefined && (borne.prix < this.prixMin || borne.prix > this.prixMax)) return false;
      if (borne.puissance !== undefined && borne.puissance < this.puissanceMin) return false;
      if (this.selectedEtat !== 'all' && borne.etat !== this.selectedEtat) return false;
      if (this.disponibleOnly && borne.etat !== 'DISPONIBLE') return false;
      return true;
    });

    if (this.showMap && this.mapInitialized) {
      this.updateMapMarkers();
    }
  }

  // ============ ACTIONS ============

  toggleView(): void {
    this.showMap = !this.showMap;
    if (this.showMap) {
      setTimeout(() => {
        if (!this.mapInitialized) this.initMap();
        else this.map?.invalidateSize();
      }, 100);
    }
  }

  onBorneSelect(borne: Borne): void {
    if (!borne.latitude || !borne.longitude) return;

    if (!this.showMap) {
      this.showMap = true;
      setTimeout(() => {
        if (!this.mapInitialized) this.initMap();
        else this.map?.invalidateSize();
        this.map?.setView([borne.latitude!, borne.longitude!], 15);
      }, 200);
    } else {
      this.map?.setView([borne.latitude, borne.longitude], 15);
    }
  }

  reserverBorne(borneId: number): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/client/mes-reservations'], { queryParams: { borneId } });
    } else {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/client/mes-reservations', borneId } });
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

  // ============ HELPERS ============

  calculateDistance(lat: number, lng: number): number {
    if (!this.userLocation) return 0;
    return this.geolocationService.calculateDistance(
      this.userLocation.lat, this.userLocation.lng, lat, lng
    );
  }

  getEtatLabel(etat: string): string {
    return this.etatLabels[etat] || etat;
  }

  getEtatBadgeClass(etat: string): string {
    return this.etatBadges[etat] || 'badge-secondary';
  }
}
