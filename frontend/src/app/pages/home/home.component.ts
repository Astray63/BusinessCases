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
    console.log('=== HOME COMPONENT: ngOnInit ===');
    this.isLoggedIn = this.authService.isLoggedIn();
    console.log('User logged in:', this.isLoggedIn);
    // Lancer la géolocalisation immédiatement
    this.getUserLocationAsync();
  }

  ngAfterViewInit(): void {
    console.log('=== HOME COMPONENT: ngAfterViewInit ===');
    // La carte sera initialisée une fois la géolocalisation obtenue
  }

  private async getUserLocationAsync(): Promise<void> {
    console.log('=== getUserLocationAsync: Début ===');
    console.log('🌍 Tentative de récupération de votre localisation réelle...');
    
    try {
      this.userLocation = await this.requestGeolocation();
      console.log('✅ GÉOLOCALISATION RÉUSSIE!');
      console.log('📍 Votre position:', this.userLocation);
      console.log('   Latitude:', this.userLocation.lat);
      console.log('   Longitude:', this.userLocation.lng);
      
      // Vérifier si c'est vraiment Paris (valeur par défaut)
      const isParis = Math.abs(this.userLocation.lat - 48.8566) < 0.01 && 
                      Math.abs(this.userLocation.lng - 2.3522) < 0.01;
      
      if (isParis) {
        console.warn('⚠️ ATTENTION: La position obtenue semble être Paris (valeur par défaut)');
        console.warn('   Cela peut signifier que:');
        console.warn('   1. Vous êtes réellement à Paris');
        console.warn('   2. La géolocalisation précise a échoué');
        console.warn('   3. Le navigateur utilise une localisation IP approximative');
      }
    } catch (error: any) {
      console.error('❌ GÉOLOCALISATION ÉCHOUÉE');
      console.error('   Raison:', error.message);
      console.warn('   → Utilisation position par défaut (Paris: 48.8566, 2.3522)');
      this.userLocation = { lat: 48.8566, lng: 2.3522 };
    }
    
    // Initialiser la carte une fois la position obtenue (réelle ou par défaut)
    this.initMap();
    this.loadBornesPubliques();
  }

  private requestGeolocation(): Promise<{ lat: number; lng: number }> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        console.warn('❌ Navigateur ne supporte pas la géolocalisation');
        reject(new Error('Géolocalisation non supportée'));
        return;
      }

      console.log('📡 Demande de géolocalisation au navigateur...');
      console.log('   Options: enableHighAccuracy=true, timeout=5000ms');
      
      const startTime = Date.now();
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const elapsed = Date.now() - startTime;
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          
          console.log('✅ Position reçue du navigateur (en ' + elapsed + 'ms):');
          console.log('   📍 Latitude:', location.lat);
          console.log('   📍 Longitude:', location.lng);
          console.log('   🎯 Précision:', position.coords.accuracy, 'mètres');
          console.log('   ⏱️  Timestamp:', new Date(position.timestamp).toLocaleString());
          
          if (position.coords.accuracy > 1000) {
            console.warn('⚠️  Précision faible (>' + position.coords.accuracy + 'm)');
            console.warn('   La position peut être approximative (basée sur IP)');
          }
          
          resolve(location);
        },
        (error) => {
          const elapsed = Date.now() - startTime;
          console.error('❌ Erreur géolocalisation (après ' + elapsed + 'ms):');
          console.error('   Code:', error.code);
          console.error('   Message:', error.message);
          
          if (error.code === 1) {
            console.error('   🚫 PERMISSION REFUSÉE');
            console.error('   → L\'utilisateur a refusé l\'accès à la localisation');
            console.error('   → Vérifiez les paramètres de votre navigateur');
          } else if (error.code === 2) {
            console.error('   📍 POSITION INDISPONIBLE');
            console.error('   → Le système n\'a pas pu déterminer votre position');
            console.error('   → Services de localisation peut-être désactivés');
          } else if (error.code === 3) {
            console.error('   ⏱️  TIMEOUT');
            console.error('   → La demande de localisation a pris trop de temps');
          }
          
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000, // Augmenté à 10 secondes
          maximumAge: 0
        }
      );
    });
  }

  getUserLocation(): void {
    // Méthode conservée pour compatibilité, mais non utilisée
    console.warn('getUserLocation() deprecated - using getUserLocationAsync() instead');
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
      const mapElement = document.getElementById('public-map');
      if (!mapElement) {
        console.error('✗ Élément DOM #public-map introuvable!');
        return;
      }
      console.log('✓ Élément DOM #public-map trouvé');

      // Vérifier si la carte existe déjà
      if (this.map) {
        console.log('Suppression de l\'ancienne carte');
        this.map.remove();
      }

      console.log('Création de la carte Leaflet...');
      this.map = L.map('public-map').setView(
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

      // Ajouter les marqueurs de bornes
      console.log('Appel de updateMapMarkers avec', this.bornesPubliques.length, 'bornes');
      this.updateMapMarkers();
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

    console.log('Nombre de bornes à afficher:', this.bornesPubliques.length);

    // Supprimer anciens marqueurs
    console.log('Suppression des anciens marqueurs:', this.markers.length);
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Ajouter nouveaux marqueurs pour chaque borne
    let marquersAjoutes = 0;
    let bornesSansCoordonnees = 0;
    
    this.bornesPubliques.forEach((borne, index) => {
      console.log(`\n--- Traitement borne ${index + 1}/${this.bornesPubliques.length} ---`);
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
    console.log('Total bornes traitées:', this.bornesPubliques.length);

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
    console.log('=== loadBornesPubliques: Chargement des bornes ===');
    this.isLoading = true;
    this.borneService.getBornesDisponibles().subscribe({
      next: (response: ApiResponse<Borne[]>) => {
        console.log('✓ Réponse API reçue:', response);
        console.log('  - Result:', response.result);
        console.log('  - Message:', response.message);
        console.log('  - Data:', response.data);
        
        this.bornesPubliques = response.data || [];
        console.log('✓ Nombre de bornes chargées:', this.bornesPubliques.length);
        
        // Afficher les détails de chaque borne
        this.bornesPubliques.forEach((borne, index) => {
          console.log(`  Borne ${index + 1}:`, {
            id: borne.idBorne,
            localisation: borne.localisation,
            latitude: borne.latitude,
            longitude: borne.longitude,
            etat: borne.etat
          });
        });
        
        this.isLoading = false;
        
        // Mettre à jour les marqueurs si la carte est déjà initialisée
        if (this.map) {
          console.log('✓ Carte existe, appel de updateMapMarkers');
          this.updateMapMarkers();
        } else {
          console.error('✗ Carte non initialisée, les marqueurs ne peuvent pas être ajoutés!');
        }
      },
      error: (error: any) => {
        console.error('✗ ERREUR lors du chargement des bornes:', error);
        console.error('  - Status:', error.status);
        console.error('  - Message:', error.message);
        console.error('  - Error:', error.error);
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