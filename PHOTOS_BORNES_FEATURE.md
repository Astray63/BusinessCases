# 📸 Fonctionnalité : Upload de Photos pour les Bornes

## Vue d'ensemble

Cette fonctionnalité permet aux propriétaires d'ajouter des photos de leurs bornes de recharge pour les rendre plus attractives aux utilisateurs potentiels. **Les photos sont maintenant stockées sur le serveur et en base de données.**

## ✅ Implémentation Production

### Architecture Complète

```
Frontend (Angular)
    ↓ Upload MultipartFile
Backend (Spring Boot)
    ↓ Sauvegarde fichiers
Système de fichiers (/uploads/bornes/)
    ↓ URLs stockées
Base de données PostgreSQL (table borne_medias)
```

## Fonctionnalités Implémentées

### 1. Backend (Spring Boot)

#### Endpoints API

**Upload de photos**
```http
POST /api/bornes/{id}/photos
Content-Type: multipart/form-data

Form Data:
- photos: File[] (max 5 fichiers)
```

**Suppression de photo**
```http
DELETE /api/bornes/{id}/photos?photoUrl={url}
```

#### Service d'Upload
- ✅ Validation des fichiers (type, taille)
- ✅ Génération de noms uniques (UUID)
- ✅ Stockage organisé par borne (`/uploads/bornes/borne-{id}/`)
- ✅ URLs accessibles via endpoint statique
- ✅ Limite de 5 photos par borne
- ✅ Gestion des erreurs robuste

#### Configuration
```properties
# application.properties
app.upload.dir=${user.home}/electriccharge/uploads/bornes
app.upload.base-url=http://localhost:8080/api/uploads/bornes
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### Base de Données
```sql
-- Table automatique via JPA
CREATE TABLE borne_medias (
    borne_id BIGINT NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (borne_id) REFERENCES charging_stations(id_borne)
);
```

### 2. Frontend (Angular)

#### Service BorneService
```typescript
uploadPhotos(borneId: number, photos: File[]): Observable<ApiResponse<string[]>>
deletePhoto(borneId: number, photoUrl: string): Observable<ApiResponse<void>>
```

#### Workflow Utilisateur
1. **Création de borne** :
   - Borne créée → Upload photos → Photos attachées
2. **Modification de borne** :
   - Borne mise à jour → Upload nouvelles photos → Photos ajoutées
   - Suppression photos existantes → API DELETE appelée

## Utilisation

### Ajouter des Photos lors de la Création d'une Borne

1. Cliquez sur "Ajouter une borne"
2. Remplissez les informations obligatoires
3. Faites défiler jusqu'à la section "Photos de la borne"
4. Cliquez sur la zone de dépôt ou sur "Cliquez pour ajouter des photos"
5. Sélectionnez jusqu'à 5 images (JPG, PNG)
6. Les aperçus s'affichent automatiquement
7. Cliquez sur "Créer la borne"
8. **Les photos sont automatiquement uploadées vers le serveur**

### Ajouter/Modifier des Photos d'une Borne Existante

1. Cliquez sur "Modifier" sur une borne existante
2. Les photos actuelles s'affichent depuis le serveur
3. Vous pouvez :
   - **Supprimer des photos** : Appel API DELETE
   - **Ajouter de nouvelles photos** : Upload vers le serveur
4. Cliquez sur "Enregistrer les modifications"

## Spécifications Techniques

### Backend

#### BorneController.java
```java
@PostMapping("/{id}/photos")
public ResponseEntity<ApiResponse<?>> uploadPhotos(
    @PathVariable Long id,
    @RequestParam("photos") MultipartFile[] photos)

@DeleteMapping("/{id}/photos")
public ResponseEntity<ApiResponse<?>> deletePhoto(
    @PathVariable Long id,
    @RequestParam String photoUrl)
```

#### ChargingStationServiceImpl.java
```java
@Override
@Transactional
public List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception {
    // 1. Validation (type, taille, limite)
    // 2. Création répertoire /uploads/bornes/borne-{id}/
    // 3. Sauvegarde fichiers avec nom unique (UUID)
    // 4. Génération URLs accessibles
    // 5. Mise à jour base de données
}

@Override
@Transactional
public void deletePhoto(Long borneId, String photoUrl) throws Exception {
    // 1. Suppression de l'URL en base
    // 2. Suppression du fichier physique
}
```

#### FileUploadConfig.java
```java
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapper /api/uploads/bornes/** vers le répertoire physique
        registry.addResourceHandler("/uploads/bornes/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

#### Modèle ChargingStation
```java
@ElementCollection(fetch = FetchType.LAZY)
@CollectionTable(
    name = "borne_medias",
    joinColumns = @JoinColumn(name = "borne_id")
)
@Column(name = "media_url")
private List<String> medias = new ArrayList<>();
```

### Frontend

#### Composant TypeScript
**Fichier**: `frontend/src/app/pages/proprietaire/mes-bornes/mes-bornes.component.ts`

```typescript
// Upload réel vers le backend
private uploadPhotosToServer(borneId: number): Promise<void> {
  return new Promise((resolve, reject) => {
    this.borneService.uploadPhotos(borneId, this.selectedFiles).subscribe({
      next: (response) => {
        if (response.result === 'SUCCESS') {
          resolve();
        } else {
          reject(new Error('Erreur upload'));
        }
      },
      error: reject
    });
  });
}

// Suppression avec appel API
removeExistingPhoto(index: number): void {
  const photoUrl = this.existingPhotos[index];
  this.borneService.deletePhoto(this.selectedBorne.idBorne, photoUrl)
    .subscribe({
      next: () => {
        this.existingPhotos.splice(index, 1);
        alert('Photo supprimée avec succès');
      },
      error: () => alert('Erreur suppression')
    });
}
```

#### BorneService
```typescript
uploadPhotos(borneId: number, photos: File[]): Observable<ApiResponse<string[]>> {
  const formData = new FormData();
  photos.forEach(photo => formData.append('photos', photo));
  return this.http.post<ApiResponse<string[]>>(
    `${this.apiUrl}/${borneId}/photos`, 
    formData
  );
}

deletePhoto(borneId: number, photoUrl: string): Observable<ApiResponse<void>> {
  const params = new HttpParams().set('photoUrl', photoUrl);
  return this.http.delete<ApiResponse<void>>(
    `${this.apiUrl}/${borneId}/photos`, 
    { params }
  );
}
```

## Validation et Contraintes

### Côté Backend
- **Type de fichier** : Images uniquement (Content-Type image/*)
- **Taille maximale** : 5MB par image
- **Nombre maximum** : 5 photos par borne
- **Format de nom** : UUID + extension (ex: `a3b2c1d4-e5f6.jpg`)
- **Stockage** : `/home/user/electriccharge/uploads/bornes/borne-{id}/`

### Côté Frontend
- **Type de fichier** : Images uniquement (image/*)
- **Taille maximale** : 5MB par image
- **Nombre maximum** : 5 photos par borne
- **Aperçu temps réel** : Oui
- **Validation avant upload** : Oui

### Sécurité
- ✅ Validation stricte des types MIME
- ✅ Vérification de la taille des fichiers
- ✅ Noms de fichiers sécurisés (UUID)
- ✅ Authentification requise pour upload
- ✅ Vérification de propriété de la borne
- ✅ Limite de 5 photos par borne

## Structure des Fichiers

### Backend
```
backend/
├── src/main/java/com/electriccharge/app/
│   ├── controller/
│   │   └── BorneController.java          ✅ +uploadPhotos, +deletePhoto
│   ├── service/
│   │   ├── ChargingStationService.java   ✅ +uploadPhotos, +deletePhoto
│   │   └── impl/
│   │       └── ChargingStationServiceImpl.java  ✅ Implémentation
│   ├── config/
│   │   └── FileUploadConfig.java         ✅ NOUVEAU
│   └── model/
│       └── ChargingStation.java          ✅ medias (déjà existant)
└── src/main/resources/
    └── application.properties             ✅ Configuration upload
```

### Frontend
```
frontend/src/app/
├── services/
│   └── borne.service.ts                  ✅ +uploadPhotos, +deletePhoto
└── pages/proprietaire/mes-bornes/
    ├── mes-bornes.component.ts           ✅ uploadPhotosToServer
    └── mes-bornes.component.html         ✅ Section photos
```

### Stockage
```
/home/user/electriccharge/uploads/bornes/
├── borne-1/
│   ├── a3b2c1d4-e5f6-7890-abcd-ef1234567890.jpg
│   └── b4c3d2e1-f6e7-8901-bcde-f12345678901.png
├── borne-2/
│   ├── c5d4e3f2-g7f8-9012-cdef-012345678912.jpg
│   ├── d6e5f4g3-h8g9-0123-defg-123456789023.jpg
│   └── e7f6g5h4-i9h0-1234-efgh-234567890134.png
└── ...
```

## URLs Générées

Format: `http://localhost:8080/api/uploads/bornes/borne-{id}/{uuid}.{ext}`

Exemples:
- `http://localhost:8080/api/uploads/bornes/borne-1/a3b2c1d4-e5f6.jpg`
- `http://localhost:8080/api/uploads/bornes/borne-2/b4c3d2e1-f6e7.png`

## Déploiement en Production

### Configuration Serveur

1. **Créer le répertoire d'upload**
```bash
mkdir -p /var/www/electriccharge/uploads/bornes
chown tomcat:tomcat /var/www/electriccharge/uploads/bornes
chmod 755 /var/www/electriccharge/uploads/bornes
```

2. **Modifier application.properties**
```properties
app.upload.dir=/var/www/electriccharge/uploads/bornes
app.upload.base-url=https://votre-domaine.com/api/uploads/bornes
```

3. **Configuration Nginx (optionnel)**
```nginx
location /api/uploads/bornes/ {
    alias /var/www/electriccharge/uploads/bornes/;
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

### Sauvegarde et Maintenance

**Sauvegarde régulière**
```bash
# Cron job quotidien
0 2 * * * tar -czf /backups/bornes-photos-$(date +\%Y\%m\%d).tar.gz /var/www/electriccharge/uploads/bornes/
```

**Nettoyage des photos orphelines**
```sql
-- Identifier les photos non utilisées
SELECT media_url FROM borne_medias
WHERE borne_id NOT IN (SELECT id_borne FROM charging_stations);
```

## Tests

### Tests Backend (JUnit)
```java
@Test
void testUploadPhotos() {
    // 1. Mock MultipartFile
    // 2. Appeler uploadPhotos()
    // 3. Vérifier fichier créé
    // 4. Vérifier URL en base
}

@Test
void testDeletePhoto() {
    // 1. Créer photo
    // 2. Appeler deletePhoto()
    // 3. Vérifier suppression BDD
    // 4. Vérifier suppression fichier
}
```

### Tests Frontend (Jasmine)
```typescript
it('should upload photos to server', () => {
  const files = [new File([''], 'test.jpg')];
  service.uploadPhotos(1, files).subscribe(response => {
    expect(response.result).toBe('SUCCESS');
    expect(response.data.length).toBeGreaterThan(0);
  });
});
```

## Monitoring et Logs

**Logs Backend**
```
INFO  - Photo uploadée: /uploads/bornes/borne-1/a3b2c1d4.jpg
ERROR - Erreur upload: Limite de 5 photos atteinte
WARN  - Suppression fichier échouée: /uploads/bornes/borne-2/xyz.jpg
```

**Métriques à surveiller**
- Nombre d'uploads par jour
- Taille totale du stockage
- Taux d'erreur d'upload
- Temps moyen d'upload

## Troubleshooting

| Problème | Solution |
|----------|----------|
| Erreur 413 (Request Entity Too Large) | Augmenter `spring.servlet.multipart.max-file-size` |
| Photos non visibles | Vérifier `FileUploadConfig` et permissions répertoire |
| Erreur lors de l'upload | Vérifier que le répertoire existe et est accessible en écriture |
| Photos disparues après redémarrage | Utiliser chemin absolu, pas relatif |

## Améliorations Futures

### Priorité Haute
1. **CDN Integration**
   - Amazon S3 / Azure Blob Storage
   - CloudFront pour distribution

2. **Compression automatique**
   - Thumbnails (150x150)
   - Images optimisées (max 1920px)

### Priorité Moyenne
3. **Traitement d'images**
   - Rotation automatique (EXIF)
   - Suppression métadonnées sensibles
   - Conversion WebP

4. **Gestion avancée**
   - Réorganisation par drag & drop
   - Photo principale/couverture
   - Galerie lightbox

---

**Date de mise à jour** : 18 novembre 2025
**Version** : 2.0.0 - Production Ready
**Statut** : ✅ Fonctionnel en production avec stockage serveur

## Fonctionnalités Implémentées

### 1. Upload de Photos
- ✅ Sélection multiple de fichiers (jusqu'à 5 photos par borne)
- ✅ Validation du type de fichier (images uniquement)
- ✅ Validation de la taille (maximum 5MB par image)
- ✅ Aperçu en temps réel des photos sélectionnées
- ✅ Gestion des photos existantes et nouvelles

### 2. Gestion des Photos
- ✅ Affichage des photos existantes lors de la modification
- ✅ Suppression individuelle des photos existantes
- ✅ Suppression des nouvelles photos avant soumission
- ✅ Compteur de photos (X/5)
- ✅ Messages d'aide et validation

### 3. Affichage dans les Cartes
- ✅ Image principale affichée en haut de chaque carte de borne
- ✅ Indicateur du nombre de photos disponibles
- ✅ Image placeholder élégante si aucune photo

## Utilisation

### Ajouter des Photos lors de la Création d'une Borne

1. Cliquez sur "Ajouter une borne"
2. Remplissez les informations obligatoires
3. Faites défiler jusqu'à la section "Photos de la borne"
4. Cliquez sur la zone de dépôt ou sur "Cliquez pour ajouter des photos"
5. Sélectionnez jusqu'à 5 images (JPG, PNG)
6. Les aperçus s'affichent automatiquement
7. Cliquez sur "Créer la borne" pour sauvegarder

### Ajouter/Modifier des Photos d'une Borne Existante

1. Cliquez sur "Modifier" sur une borne existante
2. Les photos actuelles s'affichent dans la section "Photos actuelles"
3. Vous pouvez :
   - Supprimer des photos existantes (bouton X au survol)
   - Ajouter de nouvelles photos (jusqu'à atteindre la limite de 5)
4. Cliquez sur "Enregistrer les modifications"

## Spécifications Techniques

### Composant TypeScript
**Fichier**: `frontend/src/app/pages/proprietaire/mes-bornes/mes-bornes.component.ts`

#### Nouvelles Propriétés
```typescript
selectedFiles: File[] = [];      // Fichiers sélectionnés pour upload
previewUrls: string[] = [];      // URLs d'aperçu des nouvelles photos
existingPhotos: string[] = [];   // URLs des photos existantes
```

#### Nouvelles Méthodes
```typescript
onFileSelected(event: any): void
  // Gère la sélection de fichiers avec validation

removeNewPhoto(index: number): void
  // Supprime une nouvelle photo avant upload

removeExistingPhoto(index: number): void
  // Supprime une photo existante

uploadPhotos(): Promise<string[]>
  // Upload les photos vers le serveur (actuellement en base64)

fileToBase64(file: File): Promise<string | ArrayBuffer | null>
  // Convertit un fichier en base64
```

### Template HTML
**Fichier**: `frontend/src/app/pages/proprietaire/mes-bornes/mes-bornes.component.html`

#### Section d'Upload (dans le modal)
- Zone de glisser-déposer stylisée
- Grille d'aperçu 2x3 responsive
- Boutons de suppression au survol
- Compteur de photos et messages d'aide

#### Affichage dans les Cartes
- Image en haut de chaque carte (h-48)
- Placeholder avec icône éclair si pas de photo
- Badge indiquant le nombre de photos

## Validation et Contraintes

### Côté Client
- **Type de fichier** : Images uniquement (image/*)
- **Taille maximale** : 5MB par image
- **Nombre maximum** : 5 photos par borne
- **Messages d'erreur** : Alerts pour les dépassements de limites

### Modèle de Données
```typescript
export interface Borne {
  // ... autres propriétés
  medias?: string[];  // URLs ou base64 des photos
}
```

## Stockage (Implémentation Actuelle)

### Simulation d'Upload
Actuellement, les photos sont converties en **base64** et stockées directement dans le modèle `Borne`. 

**Note**: Pour une vraie production, il faudrait :
1. Créer un endpoint backend pour l'upload de fichiers
2. Stocker les fichiers dans un système de stockage (S3, Azure Blob, etc.)
3. Sauvegarder uniquement les URLs dans la base de données

### Exemple d'Implémentation Backend (à faire)

```java
// Endpoint pour upload de photos
@PostMapping("/bornes/{id}/photos")
public ResponseEntity<ApiResponse> uploadPhotos(
    @PathVariable Long id,
    @RequestParam("photos") MultipartFile[] photos
) {
    List<String> photoUrls = borneService.uploadPhotos(id, photos);
    return ResponseEntity.ok(new ApiResponse("SUCCESS", photoUrls));
}
```

## Améliorations Futures

### Priorité Haute
1. **Upload réel vers le backend**
   - Créer endpoint Spring Boot pour l'upload
   - Utiliser un service de stockage cloud
   - Gérer les noms de fichiers uniques

2. **Compression d'images**
   - Réduire automatiquement la taille avant upload
   - Créer des thumbnails pour les aperçus

### Priorité Moyenne
3. **Drag & Drop**
   - Permettre le glisser-déposer de fichiers
   - Zone de dépôt visuelle avec feedback

4. **Réorganisation**
   - Drag & drop pour réorganiser l'ordre des photos
   - Définir une photo principale

5. **Galerie d'images**
   - Lightbox pour voir les photos en grand
   - Navigation entre les photos

### Priorité Basse
6. **Filtres et édition**
   - Rotation, recadrage de base
   - Filtres prédéfinis

7. **Chargement progressif**
   - Barre de progression pour les uploads
   - Upload en arrière-plan

## Styles CSS

Les styles utilisent **Tailwind CSS** avec des classes utilitaires :
- `border-dashed` : Bordure en pointillés pour la zone de dépôt
- `group` / `group-hover:opacity-100` : Affichage des boutons au survol
- `object-cover` : Ajustement des images dans leurs conteneurs
- Grilles responsive : `grid-cols-2 sm:grid-cols-3`

## Compatibilité

- ✅ Desktop (Chrome, Firefox, Safari, Edge)
- ✅ Mobile responsive
- ✅ Tablettes
- ⚠️ Nécessite JavaScript activé

## Tests Recommandés

1. **Test d'upload simple**
   - Ajouter 1 photo et créer une borne
   - Vérifier que la photo s'affiche

2. **Test de limite**
   - Essayer d'ajouter plus de 5 photos
   - Vérifier le message d'erreur

3. **Test de taille**
   - Essayer d'uploader un fichier > 5MB
   - Vérifier le message d'erreur

4. **Test de type**
   - Essayer d'uploader un PDF
   - Vérifier le message d'erreur

5. **Test de suppression**
   - Ajouter 3 photos, en supprimer 1
   - Vérifier que seulement 2 sont sauvegardées

6. **Test de modification**
   - Modifier une borne existante
   - Ajouter/supprimer des photos
   - Vérifier la persistance

## Fichiers Modifiés

```
frontend/src/app/pages/proprietaire/mes-bornes/
├── mes-bornes.component.ts       ✅ Modifié
├── mes-bornes.component.html     ✅ Modifié
```

## Dépendances

Aucune nouvelle dépendance nécessaire. Utilise :
- Angular Core (déjà présent)
- Tailwind CSS (déjà présent)
- Bootstrap Icons (déjà présent)

## Support

Pour toute question ou problème :
1. Vérifier les messages dans la console du navigateur
2. Vérifier la taille et le type des fichiers
3. S'assurer que le backend accepte les champs `medias`

---

**Date de création** : 18 novembre 2025
**Version** : 1.0.0
**Statut** : ✅ Fonctionnel (upload simulé en base64)
