import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { Borne } from '../models/borne.model';
import { GeolocationPosition } from './geolocation.service';

export interface MapConfig {
  containerId: string;
  center: GeolocationPosition;
  zoom?: number;
}

export interface MarkerConfig {
  position: GeolocationPosition;
  icon: L.DivIcon;
  popupContent?: string;
  onClick?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class MapService {
  private maps: Map<string, L.Map> = new Map();
  private markers: Map<string, L.Marker[]> = new Map();
  private userMarkers: Map<string, L.Marker> = new Map();

  constructor() { }

  initializeMap(config: MapConfig): L.Map | null {
    try {
      const existingMap = this.maps.get(config.containerId);
      if (existingMap) {
        existingMap.remove();
      }

      const map = L.map(config.containerId).setView(
        [config.center.lat, config.center.lng],
        config.zoom || 12
      );

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(map);

      this.maps.set(config.containerId, map);
      this.markers.set(config.containerId, []);

      return map;
    } catch (error) {
      return null;
    }
  }

  addUserMarker(mapId: string, position: GeolocationPosition, label: string = 'Votre position'): L.Marker | null {
    const map = this.maps.get(mapId);
    if (!map) return null;

    const existingMarker = this.userMarkers.get(mapId);
    if (existingMarker) {
      existingMarker.remove();
    }

    const icon = L.divIcon({
      html: '<div class="user-marker"><i class="bi bi-geo-alt-fill"></i></div>',
      className: 'custom-marker-user',
      iconSize: [32, 32],
      iconAnchor: [16, 32]
    });

    const marker = L.marker([position.lat, position.lng], { icon })
      .addTo(map)
      .bindPopup(`<strong>${label}</strong>`);

    this.userMarkers.set(mapId, marker);
    return marker;
  }

  addBorneMarkers(mapId: string, bornes: Borne[], onBorneClick?: (borne: Borne) => void): void {
    const map = this.maps.get(mapId);
    if (!map) return;

    this.clearMarkers(mapId);

    const newMarkers: L.Marker[] = [];

    bornes.forEach((borne) => {
      if (borne.latitude && borne.longitude) {
        try {
          const icon = this.createBorneIcon(borne.etat);
          const marker = L.marker([borne.latitude, borne.longitude], { icon })
            .addTo(map)
            .bindPopup(this.createBornePopup(borne));

          if (onBorneClick) {
            marker.on('click', () => onBorneClick(borne));
          }

          newMarkers.push(marker);
        } catch (error) {
          // Échec silencieux pour le marqueur individuel
        }
      }
    });

    this.markers.set(mapId, newMarkers);
    this.fitBounds(mapId);
  }

  clearMarkers(mapId: string): void {
    const markers = this.markers.get(mapId);
    if (markers) {
      markers.forEach(marker => marker.remove());
      this.markers.set(mapId, []);
    }
  }

  setView(mapId: string, position: GeolocationPosition, zoom: number = 15): void {
    const map = this.maps.get(mapId);
    if (map) {
      map.setView([position.lat, position.lng], zoom);
    }
  }

  fitBounds(mapId: string): void {
    const map = this.maps.get(mapId);
    const markers = this.markers.get(mapId);
    const userMarker = this.userMarkers.get(mapId);

    if (map && markers && markers.length > 0) {
      const allMarkers = userMarker ? [...markers, userMarker] : markers;
      const group = L.featureGroup(allMarkers);
      map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  destroyMap(mapId: string): void {
    const map = this.maps.get(mapId);
    if (map) {
      map.remove();
      this.maps.delete(mapId);
      this.markers.delete(mapId);
      this.userMarkers.delete(mapId);
    }
  }

  private createBorneIcon(etat: string): L.DivIcon {
    const colorMap: { [key: string]: string } = {
      'DISPONIBLE': '#28a745',
      'OCCUPE': '#ffc107',
      'HORS_SERVICE': '#dc3545',
      'MAINTENANCE': '#6c757d'
    };

    const color = colorMap[etat] || '#6c757d';

    return L.divIcon({
      html: `<div class="borne-marker" style="background-color: ${color};"><i class="bi bi-lightning-charge-fill"></i></div>`,
      className: 'custom-marker-borne',
      iconSize: [32, 32],
      iconAnchor: [16, 32],
      popupAnchor: [0, -32]
    });
  }

  private createBornePopup(borne: Borne & { distance?: number }): string {
    const etatLabels: { [key: string]: string } = {
      'DISPONIBLE': 'Disponible',
      'OCCUPE': 'Occupé',
      'HORS_SERVICE': 'Hors service',
      'MAINTENANCE': 'Maintenance'
    };

    const etatClasses: { [key: string]: string } = {
      'DISPONIBLE': 'badge-success',
      'OCCUPE': 'badge-warning',
      'HORS_SERVICE': 'badge-danger',
      'MAINTENANCE': 'badge-secondary'
    };

    const etatLabel = etatLabels[borne.etat] || borne.etat;
    const etatClass = etatClasses[borne.etat] || 'badge-secondary';
    const prix = borne.prix ? `${borne.prix}€/h` : 'N/A';
    const distanceHtml = borne.distance !== undefined
      ? `<p class="mb-1"><strong>Distance:</strong> ${borne.distance.toFixed(1)} km</p>`
      : '';

    return `
      <div class="popup-content">
        <h6 class="mb-2"><strong>${borne.localisation}</strong></h6>
        <span class="badge ${etatClass}">${etatLabel}</span>
        <p class="mb-1 mt-2"><strong>Type:</strong> ${borne.type}</p>
        <p class="mb-1"><strong>Puissance:</strong> ${borne.puissance} kW</p>
        <p class="mb-1"><strong>Prix:</strong> ${prix}</p>
        ${distanceHtml}
      </div>
    `;
  }
}
