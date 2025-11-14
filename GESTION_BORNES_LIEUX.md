# Gestion des Bornes et Lieux de Recharge

Cette fonctionnalité permet la gestion complète des bornes et lieux de recharge électrique avec géolocalisation.

## ✅ Fonctionnalités Implémentées

### Backend (Spring Boot)

#### 1. **CRUD Lieux de Recharge**
- ✅ **LieuDto** : DTO avec validation Jakarta (adresse, GPS, ville obligatoires)
- ✅ **LieuService & LieuServiceImpl** : Logique métier complète
- ✅ **LieuRepository** : Queries JPA + recherche par distance géographique
- ✅ **UtilisateurLieuRepository** : Gestion de la relation utilisateur-lieu
- ✅ **LieuController** : API REST complète

**Endpoints disponibles :**
```
GET    /lieux                      - Tous les lieux
GET    /lieux/{id}                 - Lieu par ID
GET    /lieux/utilisateur/{userId} - Lieux d'un utilisateur
GET    /lieux/search?nom=xxx       - Recherche par nom
GET    /lieux/proches?latitude=xxx&longitude=xxx&distance=xxx - Lieux à proximité
POST   /lieux?userId=xxx           - Créer un lieu
PUT    /lieux/{id}                 - Modifier un lieu
DELETE /lieux/{id}                 - Supprimer un lieu
```

#### 2. **CRUD Bornes de Recharge Amélioré**
- ✅ **Vérification avant suppression** : Impossible de supprimer une borne avec réservations actives
- ✅ **Endpoint utilisateur** : `GET /bornes/utilisateur/{userId}`
- ✅ **Recherche avancée** : `GET /bornes/search` avec filtres multiples

**Endpoint de recherche avancée :**
```
GET /bornes/search?latitude=xxx&longitude=xxx&distance=xxx&prixMin=xxx&prixMax=xxx&puissanceMin=xxx&etat=xxx&disponible=true
```

**Paramètres de recherche :**
- `latitude`, `longitude`, `distance` : Recherche géographique (km)
- `prixMin`, `prixMax` : Fourchette de prix (hourlyRate)
- `puissanceMin` : Puissance minimale (kW)
- `etat` : DISPONIBLE, OCCUPEE, EN_PANNE, EN_MAINTENANCE
- `disponible` : boolean (true = non occupée)

#### 3. **Calcul de Distance Géographique**
- ✅ Formule de Haversine pour calcul de distance en km
- ✅ Implémenté dans `LieuRepository.findByDistance()` et `ChargingStationRepository.findByDistance()`

### Frontend (Angular)

#### 1. **Service Lieu**
- ✅ `LieuService` : Appels HTTP vers l'API
- ✅ `Lieu` model : Interface TypeScript complète
- ✅ Méthodes : getAll, getById, getByUtilisateur, searchByNom, getProches, create, update, delete

#### 2. **Service Borne Amélioré**
- ✅ Méthode `searchBornesAdvanced()` avec tous les filtres
- ✅ Méthode `getBornesByUtilisateur()`

#### 3. **Page Gestion des Lieux**
- ✅ **LieuxComponent** : Liste + formulaire CRUD
- ✅ Formulaire réactif avec validation
- ✅ Affichage des lieux de l'utilisateur connecté
- ✅ Modification en ligne + suppression
- ✅ Design responsive avec Bootstrap
- ✅ Route : `/lieux` (protégée par AuthGuard)

## 🚀 Pour Continuer : Carte Interactive avec Leaflet

### Étape 1 : Installation de Leaflet

```bash
cd frontend
npm install leaflet @types/leaflet
```

### Étape 2 : Ajouter les styles Leaflet

Dans `frontend/angular.json`, ajouter dans `styles` :

```json
"styles": [
  "src/styles.scss",
  "node_modules/leaflet/dist/leaflet.css"
],
```

### Étape 3 : Créer le composant Map

```bash
ng generate component pages/map --module=app
```

### Étape 4 : Code du composant Map

**map.component.ts :**
```typescript
import { Component, OnInit } from '@angular/core';
import * as L from 'leaflet';
import { BorneService } from '../../services/borne.service';
import { Borne } from '../../models/borne.model';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {
  private map!: L.Map;
  private markers: L.Marker[] = [];
  
  bornes: Borne[] = [];
  
  // Filtres
  distance = 10; // km
  prixMin = 0;
  prixMax = 100;
  puissanceMin = 0;
  
  userLocation: { lat: number; lng: number } | null = null;

  constructor(private borneService: BorneService) {}

  ngOnInit(): void {
    this.getUserLocation();
  }

  private getUserLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.userLocation = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          this.initMap();
          this.searchBornes();
        },
        (error) => {
          console.error('Erreur de géolocalisation', error);
          // Position par défaut (Paris)
          this.userLocation = { lat: 48.8566, lng: 2.3522 };
          this.initMap();
          this.searchBornes();
        }
      );
    }
  }

  private initMap(): void {
    if (!this.userLocation) return;

    this.map = L.map('map').setView(
      [this.userLocation.lat, this.userLocation.lng],
      12
    );

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    // Marqueur position utilisateur
    const userIcon = L.icon({
      iconUrl: 'assets/user-marker.png',
      iconSize: [32, 32],
      iconAnchor: [16, 32]
    });

    L.marker([this.userLocation.lat, this.userLocation.lng], { icon: userIcon })
      .addTo(this.map)
      .bindPopup('Votre position')
      .openPopup();

    // Cercle de rayon
    L.circle([this.userLocation.lat, this.userLocation.lng], {
      color: 'blue',
      fillColor: '#30f',
      fillOpacity: 0.1,
      radius: this.distance * 1000
    }).addTo(this.map);
  }

  searchBornes(): void {
    if (!this.userLocation) return;

    const params = {
      latitude: this.userLocation.lat,
      longitude: this.userLocation.lng,
      distance: this.distance,
      prixMin: this.prixMin,
      prixMax: this.prixMax,
      puissanceMin: this.puissanceMin,
      disponible: true
    };

    this.borneService.searchBornesAdvanced(params).subscribe({
      next: (response) => {
        this.bornes = response.data || [];
        this.updateMarkers();
      },
      error: (error) => {
        console.error('Erreur de recherche', error);
      }
    });
  }

  private updateMarkers(): void {
    // Supprimer anciens marqueurs
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    // Ajouter nouveaux marqueurs
    this.bornes.forEach(borne => {
      if (borne.latitude && borne.longitude) {
        const marker = L.marker([borne.latitude, borne.longitude])
          .addTo(this.map)
          .bindPopup(`
            <strong>${borne.nom}</strong><br>
            ${borne.localisation}<br>
            Puissance: ${borne.puissance} kW<br>
            Prix: ${borne.hourlyRate}€/h<br>
            État: ${borne.etat}
          `);
        
        this.markers.push(marker);
      }
    });
  }

  onFilterChange(): void {
    this.searchBornes();
  }
}
```

**map.component.html :**
```html
<div class="container-fluid">
  <div class="row">
    <!-- Filtres -->
    <div class="col-md-3 p-3 bg-light">
      <h4>Filtres</h4>
      
      <div class="mb-3">
        <label>Distance (km): {{ distance }}</label>
        <input 
          type="range" 
          class="form-range" 
          min="1" 
          max="50" 
          [(ngModel)]="distance"
          (change)="onFilterChange()">
      </div>

      <div class="mb-3">
        <label>Prix min (€/h): {{ prixMin }}</label>
        <input 
          type="range" 
          class="form-range" 
          min="0" 
          max="100" 
          [(ngModel)]="prixMin"
          (change)="onFilterChange()">
      </div>

      <div class="mb-3">
        <label>Prix max (€/h): {{ prixMax }}</label>
        <input 
          type="range" 
          class="form-range" 
          min="0" 
          max="100" 
          [(ngModel)]="prixMax"
          (change)="onFilterChange()">
      </div>

      <div class="mb-3">
        <label>Puissance min (kW): {{ puissanceMin }}</label>
        <input 
          type="range" 
          class="form-range" 
          min="0" 
          max="350" 
          step="10"
          [(ngModel)]="puissanceMin"
          (change)="onFilterChange()">
      </div>

      <button class="btn btn-primary w-100" (click)="searchBornes()">
        <i class="bi bi-search"></i> Rechercher
      </button>

      <div class="mt-4">
        <h5>Résultats : {{ bornes.length }}</h5>
      </div>
    </div>

    <!-- Carte -->
    <div class="col-md-9 p-0">
      <div id="map" style="height: calc(100vh - 60px);"></div>
    </div>
  </div>
</div>
```

### Étape 5 : Ajouter la route

Dans `app-routing.module.ts` :
```typescript
{
  path: 'map',
  component: MapComponent
}
```

## 📋 Points d'Attention

### Backend
1. **Réservations actives** : La suppression d'une borne est bloquée si des réservations sont `EN_COURS` ou `CONFIRMEE` avec `dateFin >= now`
2. **Coordonnées GPS** : Les coordonnées sont optionnelles mais recommandées pour la carte
3. **Relations** : Un utilisateur peut avoir plusieurs lieux (relation Many-to-Many via `UtilisateurLieu`)

### Frontend
1. **Géolocalisation** : Demander la permission utilisateur pour `navigator.geolocation`
2. **Icônes Leaflet** : Ajouter des images personnalisées dans `assets/` pour les marqueurs
3. **Performance** : Limiter le nombre de marqueurs si trop de bornes (clustering possible)

## 🧪 Tests à Effectuer

1. ✅ Créer un lieu avec coordonnées GPS
2. ✅ Lister les lieux d'un utilisateur
3. ✅ Modifier un lieu existant
4. ✅ Tenter de supprimer une borne avec réservation active (doit échouer)
5. ⏳ Rechercher des bornes par distance
6. ⏳ Filtrer par prix et puissance
7. ⏳ Afficher les bornes sur la carte

## 📚 Documentation Technique

### Formule de Haversine (Distance)
```sql
SELECT * FROM lieu l 
WHERE (6371 * acos(
  cos(radians(:latitude)) * cos(radians(l.latitude)) * 
  cos(radians(l.longitude) - radians(:longitude)) + 
  sin(radians(:latitude)) * sin(radians(l.latitude))
)) < :distance
```
- 6371 = rayon de la Terre en km
- Résultat en kilomètres

### Structure de la Base de Données

**Table `lieu` :**
- `id_lieu` : BIGINT (PK)
- `nom` : VARCHAR(100) NOT NULL
- `adresse` : VARCHAR NOT NULL
- `latitude`, `longitude` : DOUBLE
- `created_at`, `updated_at` : TIMESTAMP

**Table `utilisateur_lieu` :**
- `utilisateur_id`, `lieu_id`, `type_adresse` : Composite PK
- `type_adresse` : ENUM ('PRINCIPALE', 'SECONDAIRE', 'TRAVAIL')

**Table `charging_stations` :**
- Champs existants + `hourlyRate` pour le tarif

## 🔄 Prochaines Étapes

1. ✅ Installation de Leaflet
2. ✅ Création du composant Map
3. ✅ Intégration de la géolocalisation
4. ✅ Affichage des marqueurs
5. ⏳ Clustering des marqueurs (si > 100 bornes)
6. ⏳ Itinéraire vers une borne (Leaflet Routing Machine)
7. ⏳ Réservation directe depuis la carte
