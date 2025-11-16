import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import * as L from 'leaflet';
import { BorneService } from '../../services/borne.service';
import { AuthService } from '../../services/auth.service';
import { Borne } from '../../models/borne.model';
import { ApiResponse } from '../../models/api-response.model';

@Component({
  selector: 'app-bornes',
  templateUrl: './bornes.component.html',
})
export class BornesComponent implements OnInit, AfterViewInit, OnDestroy {
  private map!: L.Map;
  private markers: L.Marker[] = [];
  private userMarker?: L.Marker;
  private radiusCircle?: L.Circle;
  private viewInitialized = false;
  private readonly fallbackLocation = { lat: 48.8566, lng: 2.3522 };
  
  bornes: Borne[] = [];
  filteredBornes: Borne[] = [];
  loading = false;
  errorMessage = '';
  locating = false;
  geolocationStatus: 'pending' | 'success' | 'error' = 'pending';
  geolocationMessage = '';
  
  // Filtres
  searchQuery = '';
  distance = 20; // km
  prixMin = 0;
  prixMax = 50;
  puissanceMin = 0;
  selectedEtat: string = 'all';
  disponibleOnly = true;
  
  userLocation: { lat: number; lng: number } | null = null;
  showMap = true;

  constructor(
    private borneService: BorneService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.geolocationMessage = 'Initialisation de la géolocalisation...';
  }

  ngAfterViewInit(): void {
    this.viewInitialized = true;
    // Schedule geolocation outside current change detection cycle
    setTimeout(() => this.requestInitialGeolocation(), 0);
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  retryGeolocation(): void {
    if (this.locating) {
      return;
    }
    this.requestInitialGeolocation(true);
  }

  private requestInitialGeolocation(forceRetry = false): void {
    if (!this.viewInitialized) {
      return;
    }

    this.locating = true;
    this.geolocationStatus = 'pending';
    this.geolocationMessage = forceRetry
      ? 'Nouvelle tentative de géolocalisation...'
      : 'Recherche de votre position...';

    this.resolveUserLocation()
      .then((location) => {
        console.log('🎯 Position finale utilisée:', location.lat, location.lng);
        this.userLocation = location;
        this.geolocationStatus = 'success';
        this.geolocationMessage = '';
      })
      .catch((error) => {
        console.error('❌ Échec géolocalisation, utilisation position par défaut (Paris)');
        console.error('   Erreur:', error);
        this.userLocation = { ...this.fallbackLocation };
        this.geolocationStatus = 'error';
        this.geolocationMessage = this.buildGeolocationErrorMessage(error);
      })
      .finally(() => {
        this.locating = false;
        this.initMapAndLoadData();
      });
  }

  private resolveUserLocation(): Promise<{ lat: number; lng: number }> {
    return new Promise((resolve, reject) => {
      if (!this.isGeolocationSupported()) {
        reject(new Error('GEO_UNSUPPORTED'));
        return;
      }

      if (!this.isSecureContextForGeolocation()) {
        const insecureError = new Error('GEO_INSECURE_CONTEXT');
        reject(insecureError);
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          console.log('✅ Position GPS reçue:', position.coords.latitude, position.coords.longitude);
          console.log('   Précision:', position.coords.accuracy, 'mètres');
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          console.error('❌ Erreur géolocalisation - Code:', error.code, 'Message:', error.message);
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 30000,
          maximumAge: 0
        }
      );
    });
  }

  private isGeolocationSupported(): boolean {
    return typeof navigator !== 'undefined' && !!navigator.geolocation;
  }

  private isSecureContextForGeolocation(): boolean {
    if (typeof window === 'undefined') {
      return false;
    }

    if (window.isSecureContext) {
      return true;
    }

    const hostname = window.location.hostname;
    return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '[::1]';
  }

  private buildGeolocationErrorMessage(error: any): string {
    if (error instanceof Error && error.message === 'GEO_UNSUPPORTED') {
      return 'Votre navigateur ne supporte pas la géolocalisation. Nous affichons les bornes autour de Paris.';
    }

    if (error instanceof Error && error.message === 'GEO_INSECURE_CONTEXT') {
      return 'La géolocalisation est bloquée car cette page n\'est pas servie en HTTPS. Utilisez https:// ou localhost pour l\'activer.';
    }

    if (error && typeof error === 'object' && 'code' in error) {
      const geoError = error as GeolocationPositionError;
      switch (geoError.code) {
        case geoError.PERMISSION_DENIED:
          return '🚫 Accès à la position refusé. Autorisez l\'accès à la localisation dans votre navigateur (icône à gauche de l\'URL).';
        case geoError.POSITION_UNAVAILABLE:
          return '📍 Position indisponible. Vérifiez que les services de localisation sont activés sur votre appareil.';
        case geoError.TIMEOUT:
          return '⏱️ La demande de localisation a pris trop de temps (30s). Vérifiez votre connexion GPS/WiFi ou cliquez sur "Réessayer".';
        default:
          return 'Impossible de récupérer votre position. Nous utilisons une localisation par défaut (Paris).';
      }
    }

    return 'Géolocalisation indisponible. Nous affichons les bornes autour de Paris.';
  }

  private initMapAndLoadData(): void {
    this.initMap().then(() => {
      // Charger les bornes seulement après que la carte soit initialisée
      this.searchBornes();
    });
  }

  private initMap(): Promise<void> {
    return new Promise((resolve) => {
      if (!this.userLocation) {
        resolve();
        return;
      }

      // Attendre que le DOM soit prêt
      setTimeout(() => {
        const mapElement = document.getElementById('map');
        if (!mapElement) {
          console.error('Élément #map introuvable');
          resolve();
          return;
        }

        // Supprimer l'ancienne carte si elle existe
        if (this.map) {
          this.map.remove();
        }

        this.map = L.map('map').setView(
          [this.userLocation!.lat, this.userLocation!.lng],
          12
        );

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '© OpenStreetMap'
        }).addTo(this.map);

        // Icône personnalisée pour l'utilisateur
        const userIcon = L.divIcon({
          html: '<i class="bi bi-geo-alt-fill text-primary" style="font-size: 32px;"></i>',
          className: 'custom-marker',
          iconSize: [32, 32],
          iconAnchor: [16, 32]
        });

        // Marqueur position utilisateur
        this.userMarker = L.marker([this.userLocation!.lat, this.userLocation!.lng], { icon: userIcon })
          .addTo(this.map)
          .bindPopup('<strong>Votre position</strong>')
          .openPopup();

        // Cercle de rayon
        this.updateRadiusCircle();
        
        // Résoudre la promesse une fois la carte initialisée
        resolve();
      }, 100);
    });
  }

  private updateRadiusCircle(): void {
    if (!this.userLocation || !this.map) return;
    
    if (this.radiusCircle) {
      this.radiusCircle.remove();
    }

    this.radiusCircle = L.circle([this.userLocation.lat, this.userLocation.lng], {
      color: '#007bff',
      fillColor: '#007bff',
      fillOpacity: 0.1,
      radius: this.distance * 1000,
      weight: 2
    }).addTo(this.map);
  }

  searchBornes(): void {
    if (!this.userLocation) return;

    this.loading = true;
    this.errorMessage = '';

    const params: any = {
      latitude: this.userLocation.lat,
      longitude: this.userLocation.lng,
      distance: this.distance
    };

    if (this.prixMin > 0) params.prixMin = this.prixMin;
    if (this.prixMax < 50) params.prixMax = this.prixMax;
    if (this.puissanceMin > 0) params.puissanceMin = this.puissanceMin;
    if (this.selectedEtat !== 'all') params.etat = this.selectedEtat;
    if (this.disponibleOnly) params.disponible = true;

    this.borneService.searchBornesAdvanced(params).subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        this.loading = false;
        if (response.result === 'SUCCESS' && response.data) {
          this.bornes = response.data;
          this.applyLocalFilters();
          this.updateMarkers();
        } else {
          this.errorMessage = response.message || 'Erreur lors du chargement des bornes';
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Erreur lors du chargement des bornes:', error);
        this.errorMessage = 'Impossible de charger les bornes de recharge';
      }
    });
  }

  private applyLocalFilters(): void {
    console.log('🔍 Application des filtres locaux...');
    console.log('   Recherche:', this.searchQuery);
    console.log('   Bornes totales:', this.bornes.length);
    
    // Afficher les localisations disponibles
    if (this.bornes.length > 0) {
      console.log('   📍 Localisations disponibles:');
      this.bornes.forEach((borne, index) => {
        console.log(`      ${index + 1}. "${borne.localisation}"`);
      });
    }
    
    this.filteredBornes = this.bornes.filter(borne => {
      if (!this.searchQuery) {
        return true; // Pas de recherche, on garde tout
      }
      
      const searchLower = this.searchQuery.toLowerCase();
      const localisationLower = borne.localisation?.toLowerCase() || '';
      const matches = localisationLower.includes(searchLower);
      
      if (!matches) {
        console.log(`   ✗ Borne ${borne.idBorne} exclue: "${borne.localisation}" ne contient pas "${this.searchQuery}"`);
      } else {
        console.log(`   ✓ Borne ${borne.idBorne} incluse: "${borne.localisation}" contient "${this.searchQuery}"`);
      }
      
      return matches;
    });
    
    console.log('   ✓ Bornes filtrées:', this.filteredBornes.length);
  }

  onFilterChange(): void {
    console.log('📊 onFilterChange appelé');
    console.log('   Distance:', this.distance);
    console.log('   Prix:', this.prixMin, '-', this.prixMax);
    console.log('   Puissance min:', this.puissanceMin);
    console.log('   État sélectionné:', this.selectedEtat);
    console.log('   Disponible seulement:', this.disponibleOnly);
    
    this.updateRadiusCircle();
    this.searchBornes();
  }

  onSearchChange(): void {
    console.log('🔎 onSearchChange appelé - Recherche:', this.searchQuery);
    this.applyLocalFilters();
    this.updateMarkers();
  }

  private updateMarkers(): void {
    // Vérifier si la carte est initialisée
    if (!this.map) {
      console.warn('⚠ updateMarkers: Carte non initialisée, impossible d\'ajouter des marqueurs');
      return;
    }

    // Supprimer anciens marqueurs
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Ajouter nouveaux marqueurs
    this.filteredBornes.forEach(borne => {
      if (borne.latitude && borne.longitude) {
        const icon = this.getBorneIcon(borne.etat);
        
        const marker = L.marker([borne.latitude, borne.longitude], { icon })
          .addTo(this.map)
          .bindPopup(this.createPopupContent(borne));
        
        marker.on('click', () => {
          this.onBorneSelect(borne);
        });
        
        this.markers.push(marker);
      }
    });

    // Ajuster la vue pour montrer tous les marqueurs
    if (this.markers.length > 0 && this.userMarker) {
      const group = L.featureGroup([...this.markers, this.userMarker]);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  private getBorneIcon(etat: string): L.DivIcon {
    let color = 'success';
    let icon = 'ev-station';
    
    switch(etat) {
      case 'DISPONIBLE':
        color = 'success';
        break;
      case 'OCCUPE':
        color = 'warning';
        break;
      case 'MAINTENANCE':
        color = 'secondary';
        break;
      case 'HORS_SERVICE':
        color = 'danger';
        break;
    }

    return L.divIcon({
      html: `<i class="bi bi-lightning-charge-fill text-${color}" style="font-size: 24px;"></i>`,
      className: 'custom-marker',
      iconSize: [24, 24],
      iconAnchor: [12, 24]
    });
  }

  private createPopupContent(borne: Borne): string {
    const etatBadge = this.getEtatBadgeHtml(borne.etat);
    const prix = borne.prix ? `${borne.prix}€/h` : 'N/A';
    
    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>Borne ${borne.idBorne}</strong></h6>
        <p class="mb-1"><small>${borne.localisation}</small></p>
        ${etatBadge}
        <p class="mb-1 mt-2"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        <p class="mb-2"><strong>Type:</strong> ${borne.type}</p>
        <button class="btn btn-sm btn-primary w-100" onclick="window.reserveBorne(${borne.idBorne})">
          <i class="bi bi-calendar-check"></i> Réserver
        </button>
      </div>
    `;
  }

  private getEtatBadgeHtml(etat: string): string {
    let badgeClass = 'bg-success';
    let label = 'Disponible';
    
    switch(etat) {
      case 'OCCUPE':
        badgeClass = 'bg-warning';
        label = 'Occupée';
        break;
      case 'MAINTENANCE':
        badgeClass = 'bg-secondary';
        label = 'Maintenance';
        break;
      case 'HORS_SERVICE':
        badgeClass = 'bg-danger';
        label = 'Hors service';
        break;
    }
    
    return `<span class="badge ${badgeClass}">${label}</span>`;
  }

  onBorneSelect(borne: Borne): void {
    // Centrer la carte sur la borne
    if (borne.latitude && borne.longitude && this.map) {
      this.map.setView([borne.latitude, borne.longitude], 15);
    }
  }

  reserverBorne(borneId: number | undefined): void {
    if (!borneId) {
      console.error('ID de borne invalide');
      return;
    }
    
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { 
        queryParams: { returnUrl: '/bornes' } 
      });
      return;
    }
    
    this.router.navigate(['/reservation'], { 
      queryParams: { borneId: borneId } 
    });
  }

  getEtatLabel(etat: string): string {
    switch(etat) {
      case 'DISPONIBLE': return 'Disponible';
      case 'OCCUPE': return 'Occupée';
      case 'MAINTENANCE': return 'En maintenance';
      case 'HORS_SERVICE': return 'Hors service';
      default: return etat;
    }
  }

  getEtatClass(etat: string): string {
    switch(etat) {
      case 'DISPONIBLE': return 'status-disponible';
      case 'OCCUPE': return 'status-occupee';
      case 'MAINTENANCE': return 'status-maintenance';
      case 'HORS_SERVICE': return 'status-hors-service';
      default: return '';
    }
  }

  getPuissanceLabel(puissance: number): string {
    if (puissance <= 22) return `Standard (${puissance}kW)`;
    if (puissance <= 50) return `Rapide (${puissance}kW)`;
    return `Ultra-rapide (${puissance}kW)`;
  }

  toggleView(): void {
    this.showMap = !this.showMap;
  }
} 