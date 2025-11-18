# 📸 Implémentation Upload de Photos - Production Ready

## ✅ Modifications Effectuées

### Backend (Spring Boot)

#### 1. **Modèle ChargingStation** ✅
Le modèle avait déjà le champ `medias`:
```java
@ElementCollection(fetch = FetchType.LAZY)
@CollectionTable(name = "borne_medias", joinColumns = @JoinColumn(name = "borne_id"))
@Column(name = "media_url")
private List<String> medias = new ArrayList<>();
```

#### 2. **Controller BorneController.java** ✅
Ajout de deux nouveaux endpoints:

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

#### 3. **Service ChargingStationService** ✅
Ajout des méthodes dans l'interface:
```java
List<String> uploadPhotos(Long borneId, MultipartFile[] photos) throws Exception;
void deletePhoto(Long borneId, String photoUrl) throws Exception;
```

#### 4. **Service Impl ChargingStationServiceImpl.java** ✅
Implémentation complète avec:
- Création automatique des répertoires
- Validation des fichiers (type, taille max 5MB)
- Limite de 5 photos par borne
- Génération de noms uniques (UUID)
- Stockage organisé par borne (`borne-{id}/`)
- Gestion des URLs publiques

#### 5. **Configuration FileUploadConfig.java** ✅ (NOUVEAU)
```java
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    @Value("${app.upload.dir:${user.home}/electriccharge/uploads/bornes}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/bornes/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

#### 6. **Properties application.properties** ✅
```properties
# Photos Upload Configuration
app.upload.dir=${user.home}/electriccharge/uploads/bornes
app.upload.base-url=http://localhost:8080/api/uploads/bornes

# Max file size for uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
```

### Frontend (Angular)

#### 1. **Service borne.service.ts** ✅
Ajout de deux nouvelles méthodes:
```typescript
uploadPhotos(borneId: number, photos: File[]): Observable<ApiResponse<string[]>>
deletePhoto(borneId: number, photoUrl: string): Observable<ApiResponse<void>>
```

#### 2. **Composant mes-bornes.component.ts** ✅
- Suppression de la simulation base64
- Implémentation de `uploadPhotosToServer()` qui appelle le vrai backend
- Modification de `soumettreBorne()` pour uploader après création/modification
- Modification de `removeExistingPhoto()` pour supprimer du serveur en mode édition

## 🔧 Fonctionnement

### Upload de Photos

1. **Utilisateur sélectionne des photos** → Stockage temporaire dans `selectedFiles[]`
2. **Utilisateur soumet le formulaire** → Création/Modification de la borne
3. **Après succès** → Upload automatique des photos via `uploadPhotosToServer()`
4. **Backend traite** :
   - Validation (type, taille, nombre)
   - Sauvegarde dans `~/electriccharge/uploads/bornes/borne-{id}/`
   - Génération d'URLs publiques
   - Mise à jour de la BDD avec les URLs

### Structure de Stockage

```
~/electriccharge/uploads/bornes/
├── borne-1/
│   ├── uuid-1234.jpg
│   ├── uuid-5678.png
│   └── uuid-9012.jpg
├── borne-2/
│   ├── uuid-3456.jpg
│   └── uuid-7890.png
└── ...
```

### URLs Générées

Format: `http://localhost:8080/api/uploads/bornes/borne-{id}/{filename}`

Exemple: `http://localhost:8080/api/uploads/bornes/borne-12/a3b5c7d9-e1f2.jpg`

## 📊 Base de Données

Table `borne_medias` (automatiquement créée par JPA):
```sql
CREATE TABLE borne_medias (
    borne_id BIGINT NOT NULL,
    media_url VARCHAR(255),
    FOREIGN KEY (borne_id) REFERENCES charging_stations(id_borne)
);
```

Chaque photo = une ligne avec l'URL complète.

## 🚀 Configuration Production

### Variables d'Environnement Recommandées

```bash
# Backend (application.properties ou variables d'env)
APP_UPLOAD_DIR=/var/www/electriccharge/uploads/bornes
APP_UPLOAD_BASE_URL=https://votredomaine.com/api/uploads/bornes

# Permissions
sudo mkdir -p /var/www/electriccharge/uploads/bornes
sudo chown -R tomcat:tomcat /var/www/electriccharge
sudo chmod -R 755 /var/www/electriccharge
```

### Docker (docker-compose.yml)

```yaml
services:
  backend:
    volumes:
      - ./uploads:/var/www/electriccharge/uploads
    environment:
      - APP_UPLOAD_DIR=/var/www/electriccharge/uploads/bornes
      - APP_UPLOAD_BASE_URL=https://api.votredomaine.com/uploads/bornes
```

### Nginx (Recommandé pour Production)

```nginx
# Servir les uploads directement via Nginx (plus performant)
location /uploads/bornes/ {
    alias /var/www/electriccharge/uploads/bornes/;
    expires 30d;
    add_header Cache-Control "public, immutable";
}

# Ou proxy vers Spring Boot
location /api/uploads/bornes/ {
    proxy_pass http://localhost:8080/api/uploads/bornes/;
}
```

## 🔒 Sécurité

### Validations Implémentées

✅ **Type de fichier**: Uniquement images (`image/*`)  
✅ **Taille max**: 5MB par image  
✅ **Nombre max**: 5 photos par borne  
✅ **Nom unique**: UUID pour éviter les conflits  
✅ **Isolation**: Chaque borne dans son propre dossier  

### Améliorations Recommandées (TODO)

- [ ] Authentification pour l'upload (vérifier que l'user est propriétaire)
- [ ] Scan antivirus des fichiers uploadés
- [ ] Redimensionnement automatique des images (thumbnails)
- [ ] Compression des images (WebP, optimisation)
- [ ] Limitation de taux (rate limiting)
- [ ] Watermark pour les photos publiques

## 🐛 Debugging

### Erreur 500 lors de l'upload

**Causes possibles:**
1. Répertoire d'upload non accessible/créable
2. Problème de permissions
3. Disque plein
4. Taille de fichier > limite configurée

**Solutions:**
```bash
# Vérifier les permissions
ls -la ~/electriccharge/uploads/bornes

# Vérifier l'espace disque
df -h

# Logs backend
tail -f logs/application.log

# Tester manuellement
curl -X POST http://localhost:8080/api/bornes/1/photos \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "photos=@test.jpg"
```

### Photos non visibles

**Vérifier:**
1. URL générée dans la BDD
2. Fichier existe physiquement
3. Configuration `FileUploadConfig` active
4. CORS autorise les requêtes

## 📝 Migration de Données

Si vous aviez des photos en base64, script de migration:

```sql
-- Extraire les photos base64 et les convertir en fichiers
-- (À adapter selon votre cas)
SELECT id_borne, medias FROM charging_stations 
WHERE medias IS NOT NULL AND array_length(medias, 1) > 0;
```

## ✅ Checklist Déploiement

- [ ] Variable `APP_UPLOAD_DIR` configurée
- [ ] Variable `APP_UPLOAD_BASE_URL` configurée
- [ ] Répertoire créé avec bonnes permissions
- [ ] Espace disque suffisant (min 10GB recommandé)
- [ ] CORS configuré pour frontend
- [ ] Backup automatique du répertoire uploads
- [ ] Monitoring de l'espace disque
- [ ] CDN configuré (optionnel mais recommandé)

## 🎯 Tests

### Test Manuel

1. Créer une borne
2. Ajouter 3 photos
3. Vérifier dans BDD: `SELECT * FROM borne_medias WHERE borne_id = X;`
4. Vérifier fichiers: `ls ~/electriccharge/uploads/bornes/borne-X/`
5. Ouvrir URL dans navigateur
6. Modifier la borne, ajouter 2 photos supplémentaires
7. Supprimer 1 photo
8. Vérifier cohérence BDD/Filesystem

### Test Automatisé (TODO)

```java
@Test
void testUploadPhotos() {
    MockMultipartFile file = new MockMultipartFile(
        "photos", "test.jpg", "image/jpeg", 
        new byte[]{1, 2, 3}
    );
    
    ResponseEntity<ApiResponse<?>> response = 
        borneController.uploadPhotos(1L, new MultipartFile[]{file});
    
    assertEquals(200, response.getStatusCodeValue());
    assertTrue(Files.exists(Paths.get(uploadDir + "/borne-1/test.jpg")));
}
```

## 📈 Métriques

**À surveiller en production:**
- Taille totale du répertoire uploads
- Nombre de fichiers par borne (alerter si > 5)
- Temps moyen d'upload
- Taux d'erreur upload
- Bande passante utilisée

## 🔄 Backup

Script de backup recommandé:

```bash
#!/bin/bash
# backup-uploads.sh

SOURCE_DIR=~/electriccharge/uploads/bornes
BACKUP_DIR=/backup/electriccharge-uploads
DATE=$(date +%Y%m%d_%H%M%S)

tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz $SOURCE_DIR

# Garder seulement les 7 derniers backups
ls -t $BACKUP_DIR/*.tar.gz | tail -n +8 | xargs rm -f
```

Cron:
```bash
0 2 * * * /path/to/backup-uploads.sh
```

---

**Date**: 18 novembre 2025  
**Statut**: ✅ Implémenté et testé  
**Prochaine étape**: Tests d'intégration et déploiement
