# 🔌 Electricity Business (EB)

![CI Status](https://github.com/Astray63/BusinessCases/actions/workflows/ci.yml/badge.svg)

> Plateforme web moderne connectant les propriétaires de véhicules électriques avec les propriétaires de bornes de recharge, facilitant la location et la réservation d'infrastructures de recharge pour VE.

## 📋 Table des matières
- [Présentation](#-présentation)
- [Fonctionnalités](#-fonctionnalités)
- [Stack Technique](#-stack-technique)
- [Architecture](#-architecture)
- [Démarrage](#-démarrage)
- [Développement](#-développement)
- [Tests](#-tests)
- [Déploiement](#-déploiement)
- [Documentation API](#-documentation-api)
- [Contribution](#-contribution)

## 🎯 Présentation

Electricity Business est une application web full-stack facilitant l'économie du partage pour les infrastructures de recharge de véhicules électriques. Les propriétaires de bornes peuvent mettre leurs stations en location, tandis que les conducteurs de VE peuvent découvrir, réserver et utiliser des points de recharge à proximité.

### Capacités Clés
- 🗺️ **Carte Interactive** - Trouvez des bornes près de vous avec Leaflet et requêtes spatiales PostGIS
- 📅 **Réservation Intelligente** - Réservez des créneaux avec détection de conflits et workflow d'approbation
- 💰 **Tarification Dynamique** - Les propriétaires fixent les tarifs à la minute ; calcul automatique du total
- 📊 **Tableau de Bord** - Analyses complètes pour utilisateurs et propriétaires
- 📧 **Notifications Email** - Codes de vérification et mises à jour via Brevo
- 📄 **Reçus PDF** - Factures auto-générées pour les recharges terminées
- 📈 **Export Excel** - Téléchargez l'historique des réservations et rapports
- ⭐ **Avis & Notes** - Système de feedback communautaire
- 🚨 **Signalements** - Rapportez et suivez les problèmes de bornes

## ✨ Fonctionnalités

### Pour les Conducteurs de VE
- Parcourir les bornes sur une carte interactive
- Filtrer par localisation, puissance, prix et disponibilité
- Réserver des sessions de recharge pour des créneaux horaires
- Laisser avis et notes
- Suivre l'historique des réservations
- Exporter les données vers Excel
- Signaler les problèmes de bornes

### Pour les Propriétaires de Bornes
- Enregistrer et gérer plusieurs bornes
- Définir des tarifs horaires personnalisés
- Accepter/refuser les demandes de réservation
- Consulter le tableau de bord
- Gérer les signalements

### Fonctionnalités Administratives
- Gestion des utilisateurs
- Vérification et surveillance des bornes
- Suivi de résolution des problèmes
- Analyses globales de la plateforme
- Système de notifications email

## 🛠️ Stack Technique

### Backend
- **Framework**: Spring Boot 3.2.0
- **Langage**: Java 17
- **Sécurité**: Spring Security + Authentification JWT
- **Base de données**: PostgreSQL 15 avec extension PostGIS
- **ORM**: Hibernate / Spring Data JPA
- **Build**: Maven
- **Bibliothèques**:
  - iText PDF - Génération de reçus
  - Apache POI - Exports Excel
  - API Brevo - Service d'emailing
  - JUnit 5 + Mockito - Tests

### Frontend
- **Framework**: Angular 17
- **Langage**: TypeScript 5.2
- **UI**: Tailwind CSS + Bootstrap Icons
- **Cartes**: Leaflet 1.9
- **Formulaires**: Angular Reactive Forms
- **HTTP**: Angular HttpClient avec RxJS
- **PDF**: jsPDF
- **Excel**: SheetJS (xlsx)
- **Tests**: Jasmine + Karma

### DevOps & Infrastructure
- **CI/CD**: GitHub Actions
- **Base de données**: PostGIS (PostgreSQL + extensions spatiales)
- **Conteneurisation**: Docker + Docker Compose
- **Contrôle de version**: Git
- **Tests**: 43 tests backend, 14 tests frontend (tous passants)

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Frontend Angular                            │
│  ┌────────────┐  ┌────────────┐  ┌─────────────────────┐   │
│  │  Pages     │  │ Composants │  │  Services           │    │
│  │  - Auth    │  │ - Toasts   │  │  - Auth Service     │    │
│  │  - Dashboard│  │ - Formulaires│  │ - Borne Service  │    │
│  │  - Bornes  │  │ - Carte    │  │  - Reservation Svc  │    │
│  │  - Profil  │  │ - Avis     │  │  - Email Service    │    │
│  └────────────┘  └────────────┘  └─────────────────────┘   │
└──────────────────────┬──────────────────────────────────────┘
                       │ API REST (JSON)
┌──────────────────────┴──────────────────────────────────────┐
│                  Backend Spring Boot                         │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Contrôleurs REST                       │    │
│  │  Auth │ Borne │ Réservation │ Avis │ Signalement  │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                      │
│  ┌────────────────────┴───────────────────────────────┐    │
│  │       Couche Service (Logique métier)               │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                      │
│  ┌────────────────────┴───────────────────────────────┐    │
│  │    Couche Repository (Spring Data JPA)              │    │
│  └────────────────────┬───────────────────────────────┘    │
└───────────────────────┴─────────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────────┐
│          PostgreSQL 15 + PostGIS                            │
│  Tables: utilisateur, charging_stations, reservation,      │
│          avis, signalement, lieu, borne_medias              │
└─────────────────────────────────────────────────────────────┘
```

### Structure du Projet

```
BusinessCases/
├── backend/                    # Application Spring Boot
│   ├── src/main/java/com/eb/electricitybusiness/
│   │   ├── config/            # Sécurité, CORS, etc.
│   │   ├── controller/        # Endpoints REST
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Exceptions personnalisées
│   │   ├── model/             # Entités JPA
│   │   ├── repository/        # Repositories base de données
│   │   ├── security/          # Utilitaires JWT
│   │   ├── service/           # Logique métier
│   │   │   └── impl/          # Implémentations services
│   │   ├── mapper/            # Mappeurs DTO <-> Entité
│   │   └── validator/         # Validateurs règles métier
│   ├── src/test/              # Tests unitaires & intégration
│   └── pom.xml                # Dépendances Maven
│
├── frontend/                   # Application Angular
│   ├── src/app/
│   │   ├── components/        # Composants UI réutilisables
│   │   ├── pages/             # Composants de routes
│   │   │   ├── auth/          # Connexion, Inscription
│   │   │   ├── dashboard/     # Tableau de bord
│   │   │   ├── bornes/        # Recherche de bornes
│   │   │   ├── reservation/   # Gestion réservations
│   │   │   ├── profile/       # Profil utilisateur
│   │   │   ├── proprietaire/  # Fonctionnalités propriétaire
│   │   │   └── lieux/         # Lieux/localisations
│   │   ├── models/            # Interfaces TypeScript
│   │   ├── services/          # Clients API
│   │   ├── guards/            # Guards de routes
│   │   └── interceptors/      # Intercepteurs HTTP
│   └── package.json           # Dépendances npm
│
├── database/                   # Scripts base de données
│   ├── schema.sql             # Schéma complet avec PostGIS
│   └── sample_data.sql        # Données d'exemple
│
├── .github/workflows/
│   └── ci.yml                 # Pipeline CI GitHub Actions
│
└── docker-compose.yml         # Déploiement stack complète
```

## 🚀 Démarrage

### Prérequis
- **Java**: JDK 17 ou supérieur
- **Node.js**: 20.x ou supérieur
- **PostgreSQL**: 15+ avec extension PostGIS
- **Maven**: 3.8+
- **Docker**: (optionnel, pour déploiement conteneurisé)

### Démarrage Rapide avec Docker

Le moyen le plus rapide pour lancer toute la stack :

```bash
# Cloner le dépôt
git clone https://github.com/Astray63/BusinessCases.git
cd BusinessCases

# Démarrer tous les services
docker compose up --build

# Accéder à l'application
# Frontend: http://localhost:4200
# API Backend: http://localhost:8080/api
# Base de données: localhost:5432
```

### Installation Manuelle

#### Configuration Base de Données

```bash
# Installer PostgreSQL avec PostGIS
sudo apt install postgresql-15-postgis-3  # Ubuntu/Debian
# ou
brew install postgresql postgis           # macOS

# Créer la base de données
createdb -U postgres electricity

# Activer PostGIS
psql -U postgres -d electricity -c "CREATE EXTENSION postgis;"

# Exécuter le schéma
psql -U postgres -d electricity -f database/schema.sql

# (Optionnel) Charger les données d'exemple
psql -U postgres -d electricity -f database/sample_data.sql
```

#### Configuration Backend

```bash
cd backend

# Configurer l'environnement (copier et éditer)
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Éditer application.properties avec vos paramètres:
# - Connexion base de données
# - Secret JWT
# - Clé API Brevo (pour les emails)
# - Chemins de stockage fichiers

# Compiler et lancer
mvn clean install
mvn spring-boot:run

# L'API sera disponible sur http://localhost:8080/api
```

#### Configuration Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm start

# Ou pour un build de production
npm run build

# L'application sera disponible sur http://localhost:4200
```

## 💻 Développement

### Développement Backend

#### Exécuter les Tests
```bash
cd backend
mvn test                    # Tests unitaires
mvn verify                  # Tests d'intégration + couverture
```

#### Qualité du Code
```bash
mvn clean verify            # Build + test + couverture JaCoCo
```

#### Patterns Clés
- **DTOs**: Toutes les réponses/requêtes API utilisent des DTOs (jamais exposer les entités)
- **Validation**: Bean Validation (`@Valid`, `@NotNull`, etc.)
- **Gestion d'erreurs**: `@ControllerAdvice` pour gestion globale
- **Sécurité**: Tokens JWT, contrôle d'accès basé sur les rôles
- **Transactions**: `@Transactional` sur la couche service

### Développement Frontend

#### Exécuter les Tests
```bash
cd frontend
npm test                              # Tests en mode watch
npm run test -- --watch=false --browsers=ChromeHeadless  # Mode CI
```

#### Build
```bash
npm run build                  # Build de production
npm run build -- --configuration development  # Build dev
```

#### Fonctionnalités Clés
- **Angular Moderne**: Angular 17 avec nouvelle syntaxe de contrôle de flux (`@if`, `@for`)
- **Reactive Forms**: Validation et gestion d'état des formulaires
- **Lazy Loading**: Découpage du code par routes
- **Services**: Communication API centralisée
- **Guards**: Authentification et autorisation
- **Interceptors**: Injection token JWT, gestion d'erreurs

## 🧪 Tests

### Couverture des Tests

| Composant | Tests | Statut |
|-----------|-------|--------|
| Backend   | 43    | ✅ Passants |
| Frontend  | 14    | ✅ Passants |

### Exécuter Tous les Tests

```bash
# Backend (depuis backend/)
mvn clean verify

# Frontend (depuis frontend/)
npm run test -- --watch=false --browsers=ChromeHeadless
```

### Structure des Tests
- **Tests Unitaires**: Logique de la couche service, utilitaires
- **Tests d'Intégration**: Endpoints des contrôleurs avec DB de test
- **Tests de Composants**: Composants Angular avec services mockés

## 📦 Déploiement

### Déploiement Docker

```bash
# Déploiement production
docker compose up -d

# Arrêter les services
docker compose down

# Voir les logs
docker compose logs -f [backend|frontend|db]
```

### Déploiement Manuel

#### Backend
```bash
cd backend
mvn clean package -DskipTests
java -jar target/electricity-business-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend
npm run build
# Servir dist/frontend-stable avec nginx ou votre serveur préféré
```

### Variables d'Environnement

Créer un fichier `.env` à la racine :

```env
# Base de données
POSTGRES_DB=electricity
POSTGRES_USER=postgres
POSTGRES_PASSWORD=votre_mot_de_passe_sécurisé

# JWT
JWT_SECRET=votre_clé_secrète_jwt_min_256_bits
JWT_EXPIRATION_MS=86400000

# API Email Brevo
BREVO_API_KEY=votre_clé_api_brevo
BREVO_SENDER_EMAIL=noreply@example.com
BREVO_SENDER_NAME=Electricity Business

# Stockage Fichiers
FILE_STORAGE_PATH=./storage/receipts
```

## 📚 Documentation API

### Authentification
- `POST /api/auth/register` - Inscription utilisateur avec vérification email
- `POST /api/auth/login` - Connexion avec identifiants
- `POST /api/auth/refresh` - Rafraîchir le token JWT
- `POST /api/auth/verify-email` - Vérifier l'email avec code

### Bornes de Recharge
- `GET /api/bornes` - Lister toutes les bornes
- `GET /api/bornes/{id}` - Détails d'une borne
- `GET /api/bornes/proches?lat={lat}&lng={lng}&distance={km}` - Trouver bornes à proximité
- `POST /api/bornes` - Créer une borne (propriétaire)
- `PUT /api/bornes/{id}` - Modifier une borne (propriétaire)
- `DELETE /api/bornes/{id}` - Supprimer une borne (propriétaire)

### Réservations
- `GET /api/reservations/utilisateur/{userId}` - Réservations de l'utilisateur
- `GET /api/reservations/owner/{ownerId}` - Réservations des bornes du propriétaire
- `POST /api/reservations` - Créer une réservation
- `PUT /api/reservations/{id}/accepter` - Accepter une réservation (propriétaire)
- `PUT /api/reservations/{id}/refuser` - Refuser une réservation (propriétaire)
- `DELETE /api/reservations/{id}` - Annuler une réservation
- `GET /api/reservations/{id}/receipt` - Télécharger le reçu PDF

### Avis
- `GET /api/avis/borne/{stationId}` - Obtenir les avis d'une borne
- `POST /api/avis` - Soumettre un avis
- `PUT /api/avis/{id}` - Modifier un avis
- `DELETE /api/avis/{id}` - Supprimer un avis

### Signalements
- `GET /api/signalements` - Lister tous les signalements
- `POST /api/signalements` - Signaler un problème
- `PUT /api/signalements/{id}/resolve` - Marquer un problème comme résolu

## 🎓 À Propos

Ce projet a été développé dans le cadre du **Dossier Projet pour le Titre Professionnel Concepteur Développeur d'Applications (CDA)**, niveau 6 (Bac+3/4).

Il démontre les compétences suivantes :
- ✅ **Conception d'architecture** - Architecture 3-tiers moderne avec séparation des responsabilités
- ✅ **Développement backend** - API REST avec Spring Boot, sécurité JWT, intégration services tiers
- ✅ **Développement frontend** - SPA Angular avec gestion d'état, routing, et communication HTTP
- ✅ **Bases de données** - Modélisation relationnelle complexe avec données spatiales (PostGIS)
- ✅ **Gestion de projet** - Méthode Agile, versioning Git, documentation technique
- ✅ **Qualité logicielle** - Tests unitaires et d'intégration, CI/CD, bonnes pratiques
- ✅ **DevOps** - Conteneurisation Docker, automatisation déploiement
- ✅ **Sécurité** - Authentification/autorisation, validation données, protection CSRF

---

**Développé avec ❤️ dans le cadre du Titre CDA - Spring Boot, Angular & PostgreSQL/PostGIS**