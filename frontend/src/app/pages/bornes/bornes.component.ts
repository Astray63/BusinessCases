import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import * as L from 'leaflet';
import { BorneService } from '../../services/borne.service';
import { AuthService } from '../../services/auth.service';
import { Borne } from '../../models/borne.model';
import { ApiResponse } from '../../models/api-response.model';
import { filter } from 'rxjs/operators';

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
  private geocodeTimeout?: number;
  private navigationSubscription: any;
  
  bornes: Borne[] = [];
  filteredBornes: Borne[] = [];
  loading = false;
  errorMessage = '';
  locating = false;
  geocoding = false; // Nouveau: pour indiquer qu'on est en train de géocoder
  geolocationStatus: 'pending' | 'success' | 'error' = 'pending';
  geolocationMessage = '';
  
  // Filtres
  searchQuery = '';
  distance = 1000; // km - Augmenté pour recherche nationale
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
  ) {
    // Créer une référence globale pour que le popup puisse appeler la méthode de réservation
    (window as any).reserveBorne = (borneId: number) => {
      this.reserverBorne(borneId);
    };
    
    // S'abonner aux événements de navigation pour recharger les données
    this.navigationSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        // Si on navigue vers cette route, recharger les données
        if (event.url.includes('/client/recherche') || event.url.includes('/bornes')) {
          console.log('🔄 Navigation détectée vers recherche, rechargement des données...');
          if (this.userLocation) {
            this.searchBornes();
          }
        }
      });
  }

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
    // Nettoyer la référence globale
    delete (window as any).reserveBorne;
    
    // Désabonner de la navigation
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

  retryGeolocation(): void {
    if (this.locating) {
      return;
    }
    this.requestInitialGeolocation(true);
  }

  private requestInitialGeolocation(forceRetry = false): void {
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
        
        // Initialiser la carte AVANT de charger les bornes
        if (!this.map && this.showMap && this.userLocation) {
          console.log('📍 Initialisation de la carte avec position:', this.userLocation);
          this.initMap();
        }
        
        // Charger les bornes APRÈS l'initialisation de la carte
        this.searchBornes();
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
          timeout: 10000,
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
          return '⏱️ La demande de localisation a pris trop de temps. Vérifiez votre connexion GPS/WiFi ou cliquez sur "Réessayer".';
        default:
          return 'Impossible de récupérer votre position. Nous utilisons une localisation par défaut (Paris).';
      }
    }

    return 'Géolocalisation indisponible. Nous affichons les bornes autour de Paris.';
  }

  searchBornes(): void {
    console.log('🔍 searchBornes() appelé');
    console.log('  - userLocation:', this.userLocation);
    console.log('  - map existe:', !!this.map);
    console.log('  - showMap:', this.showMap);
    
    if (!this.userLocation) {
      console.error('❌ searchBornes() annulé: userLocation est null');
      return;
    }

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

    console.log('📡 Appel API searchBornesAdvanced avec params:', params);

    this.borneService.searchBornesAdvanced(params).subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        console.log('📦 Réponse API reçue:', response);
        this.loading = false;
        if (response.result === 'SUCCESS' && response.data) {
          this.bornes = response.data;
          this.filteredBornes = [...this.bornes];
          
          console.log('✅ Bornes chargées:', this.filteredBornes.length);
          console.log('  - Première borne:', this.filteredBornes[0]);
          
          // Mettre à jour les marqueurs sur la carte
          if (this.map && this.showMap) {
            console.log('🗺️ Mise à jour de la carte...');
            this.updateMapMarkers();
            this.updateRadiusCircle();
          } else {
            console.warn('⚠️ Carte non disponible pour mise à jour');
            console.warn('  - this.map:', !!this.map);
            console.warn('  - this.showMap:', this.showMap);
          }
        } else {
          console.error('❌ Réponse API en erreur:', response);
          this.errorMessage = response.message || 'Erreur lors du chargement des bornes';
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Erreur lors du chargement des bornes:', error);
        this.errorMessage = 'Impossible de charger les bornes de recharge';
      }
    });
  }

  initMap(): void {
    console.log('=== initMap: Initialisation de la carte ===');
    if (!this.userLocation) {
      console.error('✗ Impossible d\'initialiser la carte: userLocation est null');
      return;
    }

    try {
      console.log('Position de la carte:', this.userLocation);
      
      // Vérifier si l'élément DOM existe
      const mapElement = document.getElementById('map');
      if (!mapElement) {
        console.error('✗ Élément DOM #map introuvable!');
        return;
      }
      console.log('✓ Élément DOM #map trouvé');

      // Vérifier si la carte existe déjà
      if (this.map) {
        console.log('Suppression de l\'ancienne carte');
        this.map.remove();
      }

      console.log('Création de la carte Leaflet...');
      this.map = L.map('map').setView(
        [this.userLocation.lat, this.userLocation.lng],
        12
      );
      console.log('✓ Carte créée avec succès');

      console.log('Ajout de la couche de tuiles OpenStreetMap...');
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);
      console.log('✓ Couche de tuiles ajoutée');

      // Icône personnalisée pour l'utilisateur
      const userIcon = L.divIcon({
        html: '<div class="user-marker"><i class="bi bi-geo-alt-fill"></i></div>',
        className: 'custom-marker-user',
        iconSize: [32, 32],
        iconAnchor: [16, 32]
      });

      // Marqueur position utilisateur
      console.log('Ajout du marqueur utilisateur...');
      this.userMarker = L.marker([this.userLocation.lat, this.userLocation.lng], { icon: userIcon })
        .addTo(this.map)
        .bindPopup('<strong>Votre position</strong>')
        .openPopup();
      console.log('✓ Marqueur utilisateur ajouté');

      // Ajouter le cercle de rayon
      this.updateRadiusCircle();

      // Ajouter les marqueurs de bornes s'il y en a déjà
      if (this.filteredBornes.length > 0) {
        console.log('Ajout des marqueurs de bornes existantes');
        this.updateMapMarkers();
      }
    } catch (error) {
      console.error('✗ ERREUR lors de l\'initialisation de la carte:', error);
    }
  }

  updateMapMarkers(): void {
    console.log('=== updateMapMarkers: Ajout des marqueurs ===');
    
    if (!this.map) {
      console.error('✗ ERREUR: Carte non disponible pour ajouter les marqueurs');
      return;
    }
    console.log('✓ Carte disponible');

    console.log('Nombre de bornes à afficher:', this.filteredBornes.length);

    // Supprimer anciens marqueurs
    console.log('Suppression des anciens marqueurs:', this.markers.length);
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Ajouter nouveaux marqueurs pour chaque borne
    let marquersAjoutes = 0;
    let bornesSansCoordonnees = 0;
    
    this.filteredBornes.forEach((borne, index) => {
      console.log(`\n--- Traitement borne ${index + 1}/${this.filteredBornes.length} ---`);
      console.log('  ID:', borne.idBorne);
      console.log('  Localisation:', borne.localisation);
      console.log('  Latitude:', borne.latitude, '(type:', typeof borne.latitude, ')');
      console.log('  Longitude:', borne.longitude, '(type:', typeof borne.longitude, ')');
      console.log('  État:', borne.etat);
      
      if (borne.latitude && borne.longitude) {
        try {
          const icon = this.getBorneIcon(borne.etat);
          console.log('  ✓ Icône créée pour état:', borne.etat);
          
          const marker = L.marker([borne.latitude, borne.longitude], { icon })
            .addTo(this.map)
            .bindPopup(this.createPopupContent(borne));
          
          marker.on('click', () => {
            this.selectBorne(borne);
          });
          
          this.markers.push(marker);
          marquersAjoutes++;
          console.log('  ✓ Marqueur ajouté avec succès à la position [', borne.latitude, ',', borne.longitude, ']');
        } catch (error) {
          console.error('  ✗ ERREUR lors de l\'ajout du marqueur:', error);
        }
      } else {
        bornesSansCoordonnees++;
        console.warn('  ✗ BORNE SANS COORDONNÉES VALIDES');
        console.warn('    Latitude présente:', !!borne.latitude);
        console.warn('    Longitude présente:', !!borne.longitude);
      }
    });

    console.log('\n=== RÉSUMÉ ===');
    console.log('✓ Marqueurs ajoutés avec succès:', marquersAjoutes);
    console.log('✗ Bornes sans coordonnées:', bornesSansCoordonnees);
    console.log('Total bornes traitées:', this.filteredBornes.length);

    // Ajuster la vue pour montrer tous les marqueurs
    if (this.markers.length > 0 && this.userMarker) {
      console.log('Ajustement de la vue pour afficher tous les marqueurs...');
      const group = L.featureGroup([...this.markers, this.userMarker]);
      this.map.fitBounds(group.getBounds().pad(0.1));
      console.log('✓ Vue ajustée');
    } else if (this.markers.length === 0) {
      console.warn('⚠ AUCUN MARQUEUR À AFFICHER!');
    }
  }

  updateRadiusCircle(): void {
    if (!this.map || !this.userLocation) return;

    // Supprimer l'ancien cercle
    if (this.radiusCircle) {
      this.radiusCircle.remove();
    }

    // Ajouter le nouveau cercle
    this.radiusCircle = L.circle([this.userLocation.lat, this.userLocation.lng], {
      radius: this.distance * 1000, // Convertir km en mètres
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.1,
      weight: 2
    }).addTo(this.map);
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
    const etatLabel = this.getEtatLabel(borne.etat);
    const etatClass = borne.etat === 'DISPONIBLE' ? 'success' : (borne.etat === 'OCCUPE' ? 'warning' : 'danger');
    const prix = borne.prix ? `${borne.prix}€/h` : 'N/A';
    const distance = this.userLocation && borne.latitude && borne.longitude ? 
      this.calculateDistance(borne.latitude, borne.longitude) : 0;
    
    // Préparer l'image de la borne
    const photoHtml = borne.medias && borne.medias.length > 0
      ? `<div style="width: 100%; height: 150px; overflow: hidden; border-radius: 8px; margin-bottom: 12px;">
           <img src="${borne.medias[0]}" alt="Photo de la borne" style="width: 100%; height: 100%; object-fit: cover;">
         </div>`
      : `<div style="width: 100%; height: 150px; background: linear-gradient(135deg, #e0f2fe 0%, #bfdbfe 100%); border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px;">
           <i class="bi bi-lightning-charge-fill" style="font-size: 3rem; color: #3b82f6; opacity: 0.5;"></i>
         </div>`;
    
    return `
      <div class="popup-content" style="min-width: 280px;">
        ${photoHtml}
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge badge-${etatClass}">${etatLabel}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        <p class="mb-2"><strong>Distance:</strong> ${distance.toFixed(1)} km</p>
        ${borne.medias && borne.medias.length > 1 ? `<p class="mb-2" style="font-size: 0.875rem; color: #6b7280;"><i class="bi bi-camera"></i> ${borne.medias.length} photo(s) disponible(s)</p>` : ''}
        <div class="popup-actions">
          <button class="btn btn-sm btn-primary" onclick="window.reserveBorne(${borne.idBorne})">
            <i class="bi bi-calendar-check"></i> Réserver
          </button>
        </div>
      </div>
    `;
  }

  selectBorne(borne: Borne): void {
    // Centrer la carte sur la borne sélectionnée
    if (borne.latitude && borne.longitude && this.map) {
      this.map.setView([borne.latitude, borne.longitude], 15);
    }
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

  onFilterChange(): void {
    console.log('📊 onFilterChange appelé');
    
    // Mettre à jour le rayon sur la carte
    if (this.map && this.showMap) {
      this.updateRadiusCircle();
    }
    
    // Recharger les bornes avec les nouveaux filtres
    this.searchBornes();
  }

  onSearchChange(): void {
    console.log('🔎 onSearchChange appelé - Recherche:', this.searchQuery);
    
    // Annuler le timeout précédent
    if (this.geocodeTimeout) {
      clearTimeout(this.geocodeTimeout);
    }
    
    // Si le champ est vide, ne rien faire
    if (!this.searchQuery || this.searchQuery.trim().length === 0) {
      return;
    }
    
    // Si la recherche contient du texte (minimum 3 caractères), attendre 500ms avant de géocoder
    if (this.searchQuery.trim().length >= 3) {
      this.geocodeTimeout = window.setTimeout(() => {
        this.geocodeLocation(this.searchQuery.trim());
      }, 500);
    }
  }

  private geocodeLocation(locationName: string): void {
    console.log('🌍 Géocodage de:', locationName);
    this.geocoding = true;
    
    // Utiliser l'API Nominatim d'OpenStreetMap pour le géocodage
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(locationName)}&limit=1`;
    
    fetch(url)
      .then(response => response.json())
      .then(data => {
        this.geocoding = false;
        if (data && data.length > 0) {
          const result = data[0];
          const newLocation = {
            lat: parseFloat(result.lat),
            lng: parseFloat(result.lon)
          };
          
          console.log('✅ Localisation trouvée:', newLocation);
          
          // Mettre à jour la position de l'utilisateur
          this.userLocation = newLocation;
          
          // Mettre à jour la carte
          if (this.map && this.showMap) {
            // Mettre à jour le marqueur utilisateur
            if (this.userMarker) {
              this.userMarker.setLatLng([newLocation.lat, newLocation.lng]);
            }
            // Centrer sur la nouvelle position
            this.map.setView([newLocation.lat, newLocation.lng], 12);
            // Mettre à jour le cercle de rayon
            this.updateRadiusCircle();
          }
          
          // Recharger les bornes avec la nouvelle position
          this.searchBornes();
        } else {
          console.log('❌ Aucune localisation trouvée pour:', locationName);
        }
      })
      .catch(error => {
        this.geocoding = false;
        console.error('❌ Erreur lors du géocodage:', error);
      });
  }

  onBorneSelect(borne: Borne): void {
    if (this.showMap) {
      this.showMap = true;
      // Attendre que la vue soit mise à jour
      setTimeout(() => {
        if (!this.map) {
          this.initMap();
        }
        this.selectBorne(borne);
      }, 100);
    } else {
      this.selectBorne(borne);
    }
  }

  reserverBorne(borneId: number | undefined): void {
    if (!borneId) {
      console.error('ID de borne invalide');
      return;
    }
    
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { 
        queryParams: { returnUrl: '/client/recherche' } 
      });
      return;
    }
    
    this.router.navigate(['/client/mes-reservations'], { 
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
    
    // Si on bascule vers la vue carte et qu'elle n'est pas encore initialisée
    if (this.showMap && !this.map && this.userLocation) {
      // Attendre que le DOM soit mis à jour
      setTimeout(() => {
        this.initMap();
        if (this.filteredBornes.length > 0) {
          this.updateMapMarkers();
        }
      }, 100);
    }
  }
}
